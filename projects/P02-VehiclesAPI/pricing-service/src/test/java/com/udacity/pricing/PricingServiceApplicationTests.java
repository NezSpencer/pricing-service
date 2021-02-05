package com.udacity.pricing;

import com.udacity.pricing.api.PricingController;
import com.udacity.pricing.domain.price.Price;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.math.BigDecimal;
import java.net.URI;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class PricingServiceApplicationTests {
	@Autowired
	private PricingController controller;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void contextLoads() {
		Assert.assertNotNull(controller);
	}

	@Test
	public void verifyGetPriceReturnsSuccessfulIfVehicleIdExists() throws Exception {
		mockMvc.perform(get(new URI("/services/price?vehicleId="+1))
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}

	@Test
	public void verifyGetPriceReturnsNotFoundIfVehicleIdDoesNotExist() throws Exception {
		mockMvc.perform(get(new URI("/services/price?vehicleId="+21))
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound());
	}

	private Price getPrice(){
		return new Price("NGN", BigDecimal.valueOf(1300), 1L);
	}



}
