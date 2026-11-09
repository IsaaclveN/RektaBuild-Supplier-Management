
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 **/
package org.pih.warehouse.user

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.auth.UserSignupEvent
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.User

class AuthController {

    MailService mailService
    def userService
    def authService
    GrailsApplication grailsApplication
    def recaptchaService
    def userAgentIdentService

    static allowedMethods = [login: "GET", doLogin: "POST", logout: "GET"]

    def index() {
        redirect(action: "login", params: params)
    }

    def authorized() {
        if (session.user == null) {
            flash.message = "${warehouse.message(code: 'auth.notAuthorized.message')}"
            redirect(controller: 'auth', action: 'login')
        }
    }

    def login() {
        if (session.user) {
            flash.message = "You have already logged in."
            redirect(controller: "dashboard", action: "index")
        }

        if (userAgentIdentService.isMobile()) {
            redirect(controller: "mobile", action: "login")
            return
        }
    }

    def handleLogin() {
        def userInstance = User.findByUsernameOrEmail(params.username, params.username)
        if (userInstance) {

            TimeZone userTimezone = TimeZone.getTimeZone("America/New_York")

            if (userInstance.timezone) {
                userTimezone = TimeZone.getTimeZone(userInstance.timezone)
            } else {
                String browserTimezone = request.getParameter("browserTimezone")
                if (browserTimezone != null) {
                    userTimezone = TimeZone.getTimeZone(browserTimezone)
                }
            }
            session.timezone = userTimezone

            if (!userInstance?.active) {
                flash.message = "${warehouse.message(code: 'auth.accountRequestUnderReview.message')}"
                redirect(controller: 'auth', action: 'login')
                return
            }

            if (userService.authenticate(params.username, params.password)) {
                session.user = userInstance
                session.userName = userInstance?.username

                if (userInstance?.warehouse && userInstance?.rememberLastLocation) {
                    session.warehouse = userInstance.warehouse
                }

                if (session?.targetUri) {
                    redirect(uri: session.targetUri)
                    session.targetUri = null
                    return
                }

                redirect(controller: 'dashboard', action: 'index')
            } else {
                flash.message = "${warehouse.message(code: 'auth.incorrectPassword.label', args: [params.username])}"
                userInstance = new User(username: params['username'])
                userInstance.errors.rejectValue("version", "default.authentication.failure",
                        [warehouse.message(code: 'user.label', default: 'User')] as Object[], "${warehouse.message(code: 'auth.unableToAuthenticateUser.message')}")
                render(view: "login", model: [userInstance: userInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'auth.userNotFound.message', args: [params.username])}"
            redirect(action: 'login')
        }
    }

    def logout() {
        if (session.impersonateUserId) {
            session.user = User.get(session.activeUserId)
            session.impersonateUserId = null
            session.activeUserId = null
            redirect(controller: "dashboard", action: "index")
        } else {
            flash.message = "${warehouse.message(code: 'auth.logoutSuccess.message')}"
            session.invalidate()
            redirect(action: 'login')
        }
    }

    /**
     * Allow user to register a new account
     */
    def signup() {
        Boolean enabled = grailsApplication.config.openboxes.signup.enabled ?: false

        if (!enabled) {
            flash.message = "Apologies, but the signup feature is disabled on your system. " +
                    "Please contact a system administrator for access."
            redirect(controller: "auth", action: "login")
            return
        }

        Boolean recaptchaEnabled = grailsApplication.config.openboxes.signup.recaptcha.enabled ?: false

        if (recaptchaEnabled) {
            Boolean configured = grailsApplication.config.openboxes.signup.recaptcha.v2.secretKey?.trim()

            if (!configured) {
                flash.message = "Apologies, but reCAPTCHA is not set up on this system. " +
                        "Please contact a system administrator for access."
                redirect(controller: "auth", action: "login")
                return
            }
        }
    }

    /**
     * Handle account registration.
     */
    @Transactional
    def handleSignup() {
        def userInstance = new User()

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            userInstance.properties = params

            if (params.password) {
                userInstance.password = params.password.encodeAsPassword()
                userInstance.passwordConfirm = params.passwordConfirm.encodeAsPassword()
            }

            userInstance.active = Boolean.FALSE
            userInstance.username = params.email

            Boolean recaptchaEnabled = grailsApplication.config.openboxes.signup.recaptcha.enabled ?: false

            if (recaptchaEnabled && !recaptchaService.validate(params["g-recaptcha-response"])) {
                userInstance.errors.reject("signup.recaptcha.fail.message",
                        "Nice try, robot. But your feeble attempt has failed. If you're not a robot we apologize. Please try again.")

                def exception = new ValidationException("reCAPTCHA challenge failed", userInstance.errors)
            }

            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                userService.assignDefaultRoles(userInstance)

                grailsApplication.mainContext.publishEvent(new UserSignupEvent(userInstance, params.additionalQuestions))

            } else {
                userInstance.password = params.password
                userInstance.passwordConfirm = params.passwordConfirm
                render(view: "signup", model: [userInstance: userInstance])
                return
            }
        }

        flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'user.label'), userInstance.email])}"
        redirect(action: "login")
    }

    def renderAccountCreatedEmail() {
        def userInstance = User.get(params.id)
        render(template: "/email/userAccountCreated", model: [userInstance: userInstance])
    }

    def renderAccountConfirmedEmail() {
        def userInstance = User.get(params.id)
        render(template: "/email/userAccountConfirmed", model: [userInstance: userInstance])
    }
}
