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

import java.security.MessageDigest

class ErpIntegrationAuthenticationService {

    String inventoryTokenOverride

    boolean isInventoryTokenConfigured() {
        String expectedToken = getInventoryToken()
        return expectedToken != null && !expectedToken.trim().isEmpty()
    }

    int authenticateInventoryRequest(String authorizationHeader) {
        String expectedToken = getInventoryToken()
        if (expectedToken == null || expectedToken.trim().isEmpty()) {
            return 503
        }

        def matcher = (authorizationHeader ?: "") =~ /(?i)^Bearer ([^\s]+)$/
        if (!matcher.matches()) {
            return 401
        }

        byte[] expected = expectedToken.getBytes("UTF-8")
        byte[] supplied = matcher.group(1).getBytes("UTF-8")
        return MessageDigest.isEqual(expected, supplied) ? 200 : 401
    }

    private String getInventoryToken() {
        return inventoryTokenOverride != null ? inventoryTokenOverride : System.getenv("INVENTORY_TO_SUPPLY_TOKEN")
    }
}
