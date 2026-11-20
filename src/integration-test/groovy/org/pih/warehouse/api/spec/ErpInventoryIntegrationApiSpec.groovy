/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api.spec

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.auth.ErpIntegrationAuthenticationService
import org.springframework.beans.factory.annotation.Autowired

class ErpInventoryIntegrationApiSpec extends ApiSpec {

    @Autowired
    ErpIntegrationAuthenticationService erpIntegrationAuthenticationService

    void setupData() {
        erpIntegrationAuthenticationService.inventoryTokenOverride = "integration-test-token"
    }

    void cleanupData() {
        erpIntegrationAuthenticationService.inventoryTokenOverride = null
    }

    void "health endpoint is available without a token"() {
        when:
        def response = RestAssured.given()
                .accept(ContentType.JSON)
                .get("/health")

        then:
        response.statusCode() == 200
        response.jsonPath().getString("service") == "OpenBoxes"
        response.jsonPath().getString("status") == "ok"
    }

    void "inventory product endpoint accepts a valid bearer token"() {
        when:
        def response = RestAssured.given()
                .header("Authorization", "Bearer integration-test-token")
                .header("X-Forwarded-Proto", "https")
                .accept(ContentType.JSON)
                .queryParam("max", 1000)
                .get("/integration/inventory/products")

        then:
        response.statusCode() == 200
        response.jsonPath().getInt("totalCount") >= 1
        response.jsonPath().getList("data").size() <= 100
        response.jsonPath().getString("data[0].productCode") != null
    }

    void "inventory product endpoint rejects a missing bearer token"() {
        when:
        def response = RestAssured.given()
                .header("X-Forwarded-Proto", "https")
                .accept(ContentType.JSON)
                .get("/integration/inventory/products")

        then:
        response.statusCode() == 401
        response.jsonPath().getInt("errorCode") == 401
        response.jsonPath().getString("errorMessage") == "Unauthorized"
    }

    void "inventory product endpoint rejects an invalid bearer token"() {
        when:
        def response = RestAssured.given()
                .header("Authorization", "Bearer invalid-test-token")
                .header("X-Forwarded-Proto", "https")
                .accept(ContentType.JSON)
                .get("/integration/inventory/products")

        then:
        response.statusCode() == 401
        response.jsonPath().getInt("errorCode") == 401
        response.jsonPath().getString("errorMessage") == "Unauthorized"
    }

    void "inventory product endpoint rejects plain HTTP"() {
        when:
        def response = RestAssured.given()
                .header("Authorization", "Bearer integration-test-token")
                .accept(ContentType.JSON)
                .get("/integration/inventory/products")

        then:
        response.statusCode() == 403
        response.jsonPath().getInt("errorCode") == 403
        response.jsonPath().getString("errorMessage") == "HTTPS is required"
    }
}
