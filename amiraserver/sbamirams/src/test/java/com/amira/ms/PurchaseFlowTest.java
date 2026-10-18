package com.amira.ms;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the purchase scenario from the proposal (section 6) against an in-memory database:
 * reserve, pay, deduct, cancel, restore, plus access rules and reports. The app ships with no
 * sample data, so the test creates its own catalog through the owner API.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:amiratest;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"amira.seed.demo-products=false" })
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurchaseFlowTest {
	@Autowired
	MockMvc mvc;

	// first: both tests share one database, and this one checks the app ships with no sample data
	@Test
	@Order(1)
	void startsEmpty() throws Exception {
		assertEquals(0, JsonPath.<List<?>>read(call(get("/api/product"), null, 200), "$").size());
		String owner = login("owner@amira.ph", "owner123");
		assertEquals(0, JsonPath.<List<?>>read(call(get("/api/order"), owner, 200), "$").size());
		// the owner account is the only customer record
		assertEquals(1, JsonPath.<List<?>>read(call(get("/api/customer"), owner, 200), "$").size());
		assertEquals(5, JsonPath.<List<?>>read(call(get("/api/status"), null, 200), "$").size());
	}

	@Test
	@Order(2)
	void purchaseScenario() throws Exception {
		String owner = login("owner@amira.ph", "owner123");
		String shopper = register("Test Shopper", "shopper@test.ph");
		String other = register("Other Shopper", "other@test.ph");

		LocalDate today = LocalDate.now();
		addProduct(owner, "Test Amber", "Men", variantJson("50ml", 850, 460, 40, today.plusYears(2)));
		addProduct(owner, "Test Rose", "Women",
				variantJson("50ml", 1100, 600, 18, today.plusDays(45)),
				variantJson("100ml", 1900, 1050, 3, today.plusYears(2)));
		addProduct(owner, "Test Old", "Men", variantJson("50ml", 980, 520, 30, today.minusDays(60)));

		// catalog is public and hides cost price from shoppers
		String catalog = call(get("/api/product"), null, 200);
		assertEquals(3, JsonPath.<List<?>>read(catalog, "$").size());
		assertNull(variant(catalog, "Test Amber", "50ml").get("costPrice"));
		assertNotNull(variant(call(get("/api/product"), owner, 200), "Test Amber", "50ml").get("costPrice"));

		int id = (Integer) variant(catalog, "Test Amber", "50ml").get("id");
		int stock0 = field(id, "stockQuantity");
		assertEquals(40, stock0);
		assertEquals(0, field(id, "reservedQuantity"));

		// 1) order reserves stock but does not take it
		String order = call(post("/api/order").contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"variantId\":" + id + ",\"quantity\":2}],\"shippingAddress\":\"1 Test St\"}"),
				shopper, 200);
		int orderId = JsonPath.read(order, "$.id");
		assertEquals("Pending", JsonPath.read(order, "$.statusName"));
		assertEquals("Unpaid", JsonPath.read(order, "$.paymentStatus"));
		assertEquals(2, field(id, "reservedQuantity"));
		assertEquals(stock0, field(id, "stockQuantity"));

		// someone else cannot see or pay it
		call(get("/api/order/" + orderId), other, 403);
		call(post("/api/payment").contentType(MediaType.APPLICATION_JSON)
				.content("{\"orderId\":" + orderId + ",\"method\":\"GCash\"}"), other, 403);

		// 2) payment deducts stock and moves the order to Paid
		String pay = call(post("/api/payment").contentType(MediaType.APPLICATION_JSON)
				.content("{\"orderId\":" + orderId + ",\"method\":\"GCash\"}"), shopper, 200);
		assertEquals("Completed", JsonPath.read(pay, "$.status"));
		assertEquals(stock0 - 2, field(id, "stockQuantity"));
		assertEquals(0, field(id, "reservedQuantity"));
		assertEquals("Paid", JsonPath.read(call(get("/api/order/" + orderId), shopper, 200), "$.statusName"));

		// 3) cancelling a paid order refunds it and puts stock back
		String cancelled = call(post("/api/order/" + orderId + "/cancel"), shopper, 200);
		assertEquals("Cancelled", JsonPath.read(cancelled, "$.statusName"));
		assertEquals("Refunded", JsonPath.read(cancelled, "$.paymentStatus"));
		assertEquals(stock0, field(id, "stockQuantity"));

		// 4) cancelling an unpaid order only drops the hold
		String second = call(post("/api/order").contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"variantId\":" + id + ",\"quantity\":1}],\"shippingAddress\":\"1 Test St\"}"),
				shopper, 200);
		int secondId = JsonPath.read(second, "$.id");
		assertEquals(1, field(id, "reservedQuantity"));
		call(post("/api/order/" + secondId + "/cancel"), shopper, 200);
		assertEquals(0, field(id, "reservedQuantity"));

		// 5) cannot oversell, or buy expired stock
		call(post("/api/order").contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"variantId\":" + id + ",\"quantity\":" + (stock0 + 1) + "}],"
						+ "\"shippingAddress\":\"x\"}"), shopper, 400);
		int expiredId = (Integer) variant(catalog, "Test Old", "50ml").get("id");
		call(post("/api/order").contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"variantId\":" + expiredId + ",\"quantity\":1}],\"shippingAddress\":\"x\"}"),
				shopper, 400);

		// 6) COD stays pending until the owner confirms; then the owner ships it
		String cod = call(post("/api/order").contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"variantId\":" + id + ",\"quantity\":1}],\"shippingAddress\":\"1 Test St\"}"),
				shopper, 200);
		int codId = JsonPath.read(cod, "$.id");
		String codPay = call(post("/api/payment").contentType(MediaType.APPLICATION_JSON)
				.content("{\"orderId\":" + codId + ",\"method\":\"COD\"}"), shopper, 200);
		assertEquals("Pending", JsonPath.read(codPay, "$.status"));
		int codPayId = JsonPath.read(codPay, "$.id");
		call(put("/api/payment/" + codPayId + "/confirm"), shopper, 403);
		call(put("/api/payment/" + codPayId + "/confirm"), owner, 200);
		call(put("/api/order/" + codId + "/status").contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"Shipped\"}"), owner, 200);
		call(post("/api/order/" + codId + "/cancel"), shopper, 400);
		call(put("/api/order/" + codId + "/status").contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"Paid\"}"), owner, 400);

		// access rules
		call(get("/api/order"), null, 401);
		call(get("/api/order"), shopper, 403);
		call(get("/api/report/valuation"), shopper, 403);
		call(post("/api/product").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"), shopper, 403);

		// owner reports and stock lists
		String expiring = call(get("/api/variant/expiring?days=90"), owner, 200);
		assertTrue(JsonPath.<List<String>>read(expiring, "$[*].productName").contains("Test Rose"));
		assertTrue(JsonPath.<List<String>>read(expiring, "$[*].status").contains("Expired"));
		String low = call(get("/api/variant/low-stock?threshold=5"), owner, 200);
		assertTrue(JsonPath.<List<String>>read(low, "$[*].productName").contains("Test Rose"));
		assertFalse(JsonPath.<List<?>>read(call(get("/api/report/best-sellers"), owner, 200), "$").isEmpty());
		assertFalse(JsonPath.<List<?>>read(call(get("/api/report/revenue-by-category"), owner, 200), "$").isEmpty());
		String slow = call(get("/api/report/slow-moving?months=6"), owner, 200);
		assertTrue(JsonPath.<List<String>>read(slow, "$[*].productName").contains("Test Old"));
		String valuation = call(get("/api/report/valuation"), owner, 200);
		assertTrue(((Number) JsonPath.read(valuation, "$.costValue")).doubleValue() > 0);
		assertEquals(4, JsonPath.<List<?>>read(call(get("/api/report/stock"), owner, 200), "$").size());
	}

	// ---- helpers ----

	private String variantJson(String size, int price, int cost, int stock, LocalDate expires) {
		return "{\"size\":\"" + size + "\",\"price\":" + price + ",\"costPrice\":" + cost + ",\"stockQuantity\":" + stock
				+ ",\"batchNumber\":\"T-1\",\"expiryDate\":\"" + expires + "\"}";
	}

	private void addProduct(String ownerToken, String name, String category, String... variants) throws Exception {
		call(post("/api/product").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name
				+ "\",\"category\":\"" + category + "\",\"variants\":[" + String.join(",", variants) + "]}"),
				ownerToken, 200);
	}

	private String login(String email, String password) throws Exception {
		String body = call(post("/api/customer/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"), null, 200);
		return JsonPath.read(body, "$.token");
	}

	private String register(String name, String email) throws Exception {
		String body = call(post("/api/customer/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"secret12\","
						+ "\"shippingAddress\":\"1 Test St\"}"), null, 200);
		assertFalse(body.contains("password") || body.contains("secret12"));
		return JsonPath.read(body, "$.token");
	}

	private Map<String, Object> variant(String catalog, String product, String size) {
		List<Map<String, Object>> found = JsonPath.read(catalog,
				"$[?(@.name=='" + product + "')].variants[?(@.size=='" + size + "')]");
		return found.get(0);
	}

	// reads a number off the variant (public endpoint)
	private int field(int variantId, String name) throws Exception {
		String body = call(get("/api/variant/" + variantId), null, 200);
		return JsonPath.read(body, "$." + name);
	}

	private String call(MockHttpServletRequestBuilder request, String token, int expected) throws Exception {
		if (token != null) {
			request.header("Authorization", "Bearer " + token);
		}
		MvcResult result = mvc.perform(request).andExpect(status().is(expected)).andReturn();
		return result.getResponse().getContentAsString();
	}
}
