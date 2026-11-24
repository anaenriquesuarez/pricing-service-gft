package com.inditex.pricing.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import io.restassured.response.ValidatableResponse;

/** Full HTTP tests against the embedded server, with real Spring + H2. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PriceApiE2ETest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/prices";
    }

    private void assertPrice(String date, int tariff, String expectedPrice) {
        ValidatableResponse response = given()
                .queryParam("applicationDate", date)
                .queryParam("productId", 35455)
                .queryParam("brandId", 1)
        .when()
                .get()
        .then()
                .statusCode(200)
                .body("productId", equalTo(35455))
                .body("brandId", equalTo(1))
                .body("priceList", equalTo(tariff))
                .body("currency", equalTo("EUR"));

        BigDecimal actualPrice = new BigDecimal(response.extract().jsonPath().getString("price"));
        assertEquals(0, new BigDecimal(expectedPrice).compareTo(actualPrice));
    }

    @Test void basePriceMorning14th() { assertPrice("2020-06-14T10:00:00", 1, "35.50"); }
    @Test void promotionAfternoon14th() { assertPrice("2020-06-14T16:00:00", 2, "25.45"); }
    @Test void basePriceEvening14th() { assertPrice("2020-06-14T21:00:00", 1, "35.50"); }
    @Test void promotionMorning15th() { assertPrice("2020-06-15T10:00:00", 3, "30.50"); }
    @Test void promotionEvening16th() { assertPrice("2020-06-16T21:00:00", 4, "38.95"); }
}
