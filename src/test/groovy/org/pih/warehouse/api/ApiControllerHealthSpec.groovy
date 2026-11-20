/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONObject
import org.pih.warehouse.product.Product
import spock.lang.Specification

class ApiControllerHealthSpec extends Specification implements DataTest, ControllerUnitTest<ApiController> {

    void setupSpec() {
        mockDomains(Product)
    }

    void "health returns a minimal service status"() {
        when:
        controller.health()

        then:
        controller.response.status == 200
        JSONObject json = new JSONObject(controller.response.contentAsString)
        json.service == "OpenBoxes"
        json.status == "ok"
        json.size() == 2
    }
}
