/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.auth

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ErpIntegrationAuthenticationServiceSpec extends Specification implements ServiceUnitTest<ErpIntegrationAuthenticationService> {

    void "accepts the configured inventory bearer token"() {
        given:
        service.inventoryTokenOverride = "test-service-token"

        expect:
        service.authenticateInventoryRequest("Bearer test-service-token") == 200
    }

    void "rejects a missing inventory bearer token"() {
        given:
        service.inventoryTokenOverride = "test-service-token"

        expect:
        service.authenticateInventoryRequest(null) == 401
    }

    void "rejects an invalid inventory bearer token"() {
        given:
        service.inventoryTokenOverride = "test-service-token"

        expect:
        service.authenticateInventoryRequest("Bearer wrong-token") == 401
    }

    void "fails closed when the inventory token is not configured"() {
        given:
        service.inventoryTokenOverride = ""

        expect:
        service.authenticateInventoryRequest("Bearer test-service-token") == 503
    }
}
