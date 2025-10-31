package com.xuwei.controller;

import com.xuwei.AbstractIT;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest extends AbstractIT {

    @Test
    void shouldReturnAllTasks() {
        String body = given()
                .when()
                .get("/api/tasks/active")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonPath jp = JsonPath.from(body);
        List<String> names = jp.getList("name", String.class);

        List<String> expected = List.of(
                "Express Delivery",
                "Help Buy",
                "Document Pickup",
                "Food Delivery",
                "Grocery Shopping",
                "Pharmacy Delivery",
                "Large Parcel Delivery",
                "Night Express"
        );

        for (String e : expected) {
            assertTrue(names.contains(e), "Expected task name not " +
                    "found: " + e);
        }
        assertTrue(names.size() >= expected.size(), "Expected at " +
                "least " + expected.size() + " tasks, but found " + names.size());
    }

}
