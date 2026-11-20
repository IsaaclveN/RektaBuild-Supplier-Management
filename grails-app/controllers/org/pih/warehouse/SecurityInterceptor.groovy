package org.pih.warehouse
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import org.pih.warehouse.auth.AuthService
import grails.converters.JSON
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationStatus

class SecurityInterceptor {

    static ArrayList controllersWithAuthUserNotRequired = ['test', 'errors']
    static ArrayList actionsWithAuthUserNotRequired = ['status', 'health', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo', 'changeLocation', 'menu']

    static ArrayList controllersWithLocationNotRequired = ['categoryApi', 'productApi', 'genericApi', 'api']
    static ArrayList actionsWithLocationNotRequired = ['status', 'health', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo', 'chooseLocation', 'menu']

    def authService
    def erpIntegrationAuthenticationService

    public SecurityInterceptor() {
        matchAll().except(uri: '/static/**').except(controller: "errors").except(uri: "/info").except(uri: "/health")
    }

    void afterView() {
        // Clear out current user after rendering the view
        authService.currentUser = null
        authService.currentLocation = null
    }
    boolean before() {

        // set user/warehouse, if present, or clear them if not
        authService.currentUser = session.user ?: null
        authService.currentLocation = session.warehouse ?: null

        if (applicationRelativeRequestPath() == "/api/integration/inventory/products") {
            if (!isSecureInventoryRequest()) {
                response.status = 403
                render([errorCode: 403, errorMessage: "HTTPS is required"] as JSON)
                return false
            }

            int authenticationStatus = erpIntegrationAuthenticationService.authenticateInventoryRequest(
                request.getHeader("Authorization")
            )
            if (authenticationStatus == 503) {
                response.status = 503
                render([errorCode: 503, errorMessage: "Service authentication is not configured"] as JSON)
                return false
            }
            if (authenticationStatus != 200) {
                response.status = 401
                render([errorCode: 401, errorMessage: "Unauthorized"] as JSON)
                return false
            }

            if (!prepareInventoryProductQuery()) {
                return false
            }

            request.setAttribute("erp.integration.inventory.authenticated", true)
            params.format = "inventoryIntegration"
            params.includeInactive = false
            Integer requestedMax = params.int("max")
            Integer requestedOffset = params.int("offset")
            params.max = Math.min(Math.max(requestedMax == null ? 50 : requestedMax, 1), 100)
            params.offset = Math.max(requestedOffset == null ? 0 : requestedOffset, 0)
            return true
        }

        // This allows requests for the health monitoring endpoint to pass through without a user
        if (controllerName.equals("api") && actionName.equals("status")) {
            return true
        }

        // This allows the megamenu to be g:include'd in the page (allowing for dynamic content to be added)
        if (controllerName.equals("dashboard") && actionName.equals("megamenu")) {
            return true
        }

        // This allows the menu to be g:include'd on mobile page (allowing for dynamic content to be added)
        if (controllerName.equals("mobile") && actionName.equals("menu")) {
            return true
        }

        // Not sure when this happens
        if (params.controller == null) {
            redirect(controller: 'auth', action: 'login')
            return true
        }
        // When a request does not require authentication, we return true
        // FIXME In order to start working on sync use cases, we need to authenticate
        else if (controllersWithAuthUserNotRequired.contains(controllerName)) {
            return true
        }
        // When there's no authenticated user in the session and a request requires authentication
        // we redirect to the auth login page.  targetUri is the URI the user was trying to get to.
        else if (!session.user && !(actionsWithAuthUserNotRequired.contains(actionName))) {
            def targetUri = ""
            // We only want to handle GETs because POSTs would be much more difficult
            if (request.method == "GET") {
                targetUri = (request.forwardURI - request.contextPath)
                if (request.queryString)
                    targetUri += "?" + request.queryString
            }

            // Prevent user from being redirected to invalid pages after re-authenticating
            if (!targetUri.contains("/dashboard/status") && !targetUri.contains("logout")) {
                log.info "Request requires authentication, saving targetUri = " + targetUri
                if (targetUri != "/") {
                    flash.message = "Your session has timed out."
                }
                session.targetUri = targetUri
            } else {
                log.info "Not saving targetUri " + targetUri
            }

            if (RequestUtil.isAjax(request)) {
                redirect(controller: "errors", action: "handleUnauthorized")
                return false
            }

            redirect(controller: 'auth', action: 'login')
            return false
        }

        // When a user has been authenticated, we want to check if they have an active account
        if (session?.user && !session?.user?.active) {
            session.user = null

            if (RequestUtil.isAjax(request)) {
                redirect(controller: "errors", action: "handleUnauthorized")
                return false
            }

            redirect(controller: 'auth', action: 'login')
            return false
        }

        // Check if a user is logged to disabled location (location.active or organization.active is false) - if so, redirect to location chooser
        Location currentLocation = authService.currentLocation
        if (currentLocation && (currentLocation.status != LocationStatus.ENABLED)) {
            flash.message = "Your location is disabled (either the associated organization or your location itself became inactive)"
            session.warehouse = null
        }

        // When a user has not selected a warehouse and they are requesting an action that requires one,
        // we redirect to the choose warehouse page.
        if (!session.warehouse && !(actionsWithLocationNotRequired.contains(actionName) ||
            controllersWithLocationNotRequired.contains(controllerName) || controllerName.endsWith("Api"))) {

            session.warehouseStillNotSelected = true
            log.info "Request ${controllerName}:${actionName} requires location, redirecting to chooseLocation ..."
            redirect(controller: 'dashboard', action: 'chooseLocation')
            return false
        }

        return true
    }

    private String applicationRelativeRequestPath() {
        String path = request.forwardURI ?: request.requestURI
        String contextPath = request.contextPath ?: ""
        return contextPath && path.startsWith(contextPath) ? path.substring(contextPath.length()) : path
    }

    private boolean isSecureInventoryRequest() {
        return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))
    }

    private boolean prepareInventoryProductQuery() {
        Set<String> allowedParameters = ["q", "max", "offset"] as Set<String>
        boolean hasUnsupportedParameter = request.parameterMap.keySet().any { !allowedParameters.contains(it) }
        String maxValue = request.getParameter("max")
        String offsetValue = request.getParameter("offset")
        String query = request.getParameter("q")
        boolean hasInvalidPaging = (maxValue && !(maxValue ==~ /\d{1,9}/)) ||
                (offsetValue && !(offsetValue ==~ /\d{1,9}/))
        boolean hasLongQuery = query && query.length() > 200

        if (hasUnsupportedParameter || hasInvalidPaging || hasLongQuery) {
            response.status = 400
            render([errorCode: 400, errorMessage: "Invalid query parameters"] as JSON)
            return false
        }
        return true
    }
}
