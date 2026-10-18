package com.amira.ms.config;

import com.amira.ms.entity.CustomerData;
import com.amira.ms.entity.ProductData;
import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.entity.StatusData;
import com.amira.ms.repository.CustomerDataRepository;
import com.amira.ms.repository.ProductDataRepository;
import com.amira.ms.repository.ProductVariantDataRepository;
import com.amira.ms.repository.StatusDataRepository;
import com.amira.ms.service.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Creates only what the app cannot run without: the order statuses and the owner account.
 * Customers and orders are never added. Products are added only when amira.seed.demo-products
 * is true and the product table is empty. Safe to run on every start.
 */
@Component
public class DataSeeder implements CommandLineRunner {
	Logger logger = LoggerFactory.getLogger(DataSeeder.class);

	@Autowired
	StatusDataRepository statusRepository;

	@Autowired
	CustomerDataRepository customerRepository;

	@Value("${amira.seed.owner-email}")
	String ownerEmail;

	@Value("${amira.seed.owner-password}")
	String ownerPassword;

	@Autowired
	ProductDataRepository productRepository;

	@Autowired
	ProductVariantDataRepository variantRepository;

	// off by default; docker-compose.yml turns it on so a fresh stack has a shop to show
	@Value("${amira.seed.demo-products:false}")
	boolean demoProducts;

	// name, brand, category, photo in the Angular assets/products folder, 50ml price in PHP.
	// The first four are the ones the home page features.
	private static final String[][] DEMO = {
		{"Sauvage", "Dior", "Men", "sauvage.jpg", "5200"},
		{"J'adore", "Dior", "Women", "jadore.png", "5600"},
		{"Miss Dior Eau de Parfum", "Dior", "Women", "miss-dior-edp.jpg", "5400"},
		{"Bleu de Chanel Eau de Parfum", "Chanel", "Men", "bleu-de-chanel-edp.jpg", "6200"},
		{"Chanel No. 5 Eau de Parfum", "Chanel", "Women", "chanel-no5-edp.jpg", "6600"},
		{"Acqua di Gio", "Giorgio Armani", "Men", "acqua-di-gio.jpg", "3900"},
		{"Black Opium", "Yves Saint Laurent", "Women", "black-opium.png", "4800"},
		{"Libre", "Yves Saint Laurent", "Women", "libre.jpg", "4900"},
		{"Eros", "Versace", "Men", "eros.png", "3400"},
		{"Good Girl", "Carolina Herrera", "Women", "good-girl.jpg", "5100"},
		{"1 Million", "Paco Rabanne", "Men", "1-million.png", "3800"},
		{"Invictus", "Paco Rabanne", "Men", "invictus.jpg", "3700"},
		{"La Vie Est Belle", "Lancome", "Women", "la-vie-est-belle.jpg", "4700"},
		{"Born in Roma Donna", "Valentino", "Women", "donna-born-in-roma.jpg", "5000"},
		{"Daisy Wild", "Marc Jacobs", "Women", "daisy-wild.jpg", "3600"},
		{"Paradoxe", "Prada", "Women", "paradoxe.jpg", "5300"},
		{"Polo Blue", "Ralph Lauren", "Men", "polo-blue.jpg", "3300"},
		{"Only The Brave", "Diesel", "Men", "only-the-brave.jpg", "2900"},
		{"CK One", "Calvin Klein", "Unisex", "ck-one.jpg", "2600"}
	};

	private void seedDemoProducts() {
		LocalDate today = LocalDate.now();
		for (String[] d : DEMO) {
			ProductData p = new ProductData();
			p.setName(d[0]);
			p.setBrand(d[1]);
			p.setCategory(d[2]);
			p.setDescription(d[1] + " " + d[0] + ", a " + d[2].toLowerCase() + " fragrance.");
			p.setImageUrl("assets/products/" + d[3]);
			p.setDateAdded(today);
			p = productRepository.save(p);
			BigDecimal price50 = new BigDecimal(d[4]);
			addVariant(p.getId(), "50ml", price50, 20, today);
			addVariant(p.getId(), "100ml", price50.multiply(new BigDecimal("1.6")).setScale(0, RoundingMode.HALF_UP), 12, today);
		}
		logger.info("Added {} starter products", DEMO.length);
	}

	private void addVariant(int productId, String size, BigDecimal price, int stock, LocalDate today) {
		ProductVariantData v = new ProductVariantData();
		v.setProductId(productId);
		v.setSize(size);
		v.setSku("AM-" + productId + "-" + size.toUpperCase());
		v.setPrice(price);
		v.setCostPrice(price.multiply(new BigDecimal("0.6")).setScale(2, RoundingMode.HALF_UP));
		v.setStockQuantity(stock);
		v.setBatchNumber("B-" + today.getYear() + "-01");
		v.setManufactureDate(today.minusMonths(2));
		v.setExpiryDate(today.plusYears(3));
		v.setStatus(Rules.ACTIVE);
		variantRepository.save(v);
	}

	@Override
	public void run(String... args) {
		for (String name : List.of(Rules.PENDING, Rules.PAID, Rules.SHIPPED, Rules.DELIVERED, Rules.CANCELLED)) {
			if (statusRepository.findByName(name).isEmpty()) {
				StatusData status = new StatusData();
				status.setName(name);
				statusRepository.save(status);
			}
		}
		if (!customerRepository.existsByRole(Rules.OWNER)) {
			CustomerData owner = new CustomerData();
			owner.setName("AMIRA Owner");
			owner.setEmail(ownerEmail);
			owner.setRole(Rules.OWNER);
			owner.setPasswordHash(new BCryptPasswordEncoder().encode(ownerPassword));
			customerRepository.save(owner);
			logger.info("Created owner account {}", ownerEmail);
		}
		if (demoProducts && productRepository.count() == 0) {
			seedDemoProducts();
		}
	}
}
