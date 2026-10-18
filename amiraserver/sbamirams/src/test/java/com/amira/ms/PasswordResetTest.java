package com.amira.ms;

import com.amira.ms.config.ResetLinkSender;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Forgot password: the emailed link works once, expires, and never reveals which emails exist. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:amirareset;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"amira.seed.demo-products=false" })
@AutoConfigureMockMvc
class PasswordResetTest {
	@Autowired
	MockMvc mvc;

	@MockBean
	ResetLinkSender sender;

	@Test
	void resetFlow() throws Exception {
		String oldSession = JsonPath.read(
				json("/api/customer/register", "{\"name\":\"Reset Me\",\"email\":\"reset@test.ph\",\"password\":\"oldpass1\"}", 200),
				"$.token");
		call(get("/api/order/customer/2").header("Authorization", "Bearer " + oldSession), 200);

		// unknown email: same answer as a real one, and nothing is sent
		String unknown = json("/api/customer/forgot-password", "{\"email\":\"nobody@test.ph\"}", 200);
		verify(sender, never()).send(any(), any());

		// known email: the answer is identical, and a link goes out (case of the email does not matter)
		String known = json("/api/customer/forgot-password", "{\"email\":\"Reset@Test.ph\"}", 200);
		assertEquals(unknown, known);
		ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
		verify(sender).send(any(), token.capture());

		// bad link and weak password are rejected
		json("/api/customer/reset-password", "{\"token\":\"not-a-real-token\",\"password\":\"newpass1\"}", 400);
		json("/api/customer/reset-password", "{\"token\":\"" + token.getValue() + "\",\"password\":\"123\"}", 400);

		// the real link sets the new password
		json("/api/customer/reset-password", "{\"token\":\"" + token.getValue() + "\",\"password\":\"newpass1\"}", 200);
		json("/api/customer/login", "{\"email\":\"reset@test.ph\",\"password\":\"oldpass1\"}", 401);
		json("/api/customer/login", "{\"email\":\"reset@test.ph\",\"password\":\"newpass1\"}", 200);

		// the link cannot be used twice, and the old session was ended
		json("/api/customer/reset-password", "{\"token\":\"" + token.getValue() + "\",\"password\":\"another1\"}", 400);
		call(get("/api/order/customer/2").header("Authorization", "Bearer " + oldSession), 401);
	}

	private String json(String url, String body, int expected) throws Exception {
		return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
	}

	private void call(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, int expected)
			throws Exception {
		mvc.perform(request).andExpect(status().is(expected));
	}
}
