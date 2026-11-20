# ERP Integration Readiness

## Current state

The repository is OpenBoxes, a Grails 3.3.16/Groovy 2.4 application, not Laravel/PHP. Its role in this ERP is assumed to be **Supplier/Supply** from the supplied labels; confirm the official module name before deployment. The Supplier public hostname, frontend origins, and actual deployment database are still unknown.

The checked-in `application.yml` sets `server.contextPath` to `/openboxes`. External Grails configuration can override it, so this is the repository default, **not confirmation of the live EC2 context path**. If unchanged, the API root is `https://<SUPPLIER_HOST>/openboxes/api`.

| Area | Finding |
| --- | --- |
| Routes | 223 `/api` route patterns, covering catalog/products, inventory, stock movement, purchasing, receiving, invoices, and other functionality. |
| Authentication | Existing browser APIs continue to use username/password login and `JSESSIONID` sessions with role checks. A separate Inventory Bearer token is now required only for the read-only `/api/integration/inventory/products` endpoint. |
| Middleware | Security and role interceptors described above; preserve them for all business actions. |
| CORS | No active origin allowlist found. The existing guide demonstrates wildcard CORS and must not be copied into production. CORS is not needed for server-to-server traffic. |
| Outbound HTTP | `ApiClientService` exists and uses Apache HttpClient, but only sets a 10-second connect timeout. It does not consistently handle status codes, response timeouts, retries, or safe dependency errors. No peer ERP clients found. |
| Environment | The inbound Inventory token is read from `INVENTORY_TO_SUPPLY_TOKEN`. Peer outbound URL/token variables are not wired into calls. Use deployment secrets/configuration, not tracked source. |
| Inventory host check | The supplied `http://3.1.27.247:8005/api/health` returned HTTP 200 with `{"service":"Laravel","status":"ok"}`. HTTPS to the same host/port failed TLS negotiation (`wrong version number`); no credentials were sent. |
| Documentation/tests | Existing docs under `docs/api-guide/`; unit and integration tests under `src/test` and `src/integration-test`. |
| Health | Existing unauthenticated `/api/status` reports database diagnostics. Added `/api/health` returns a minimal JSON health body without database details. |
| Proxy | Example Nginx config under `docker/nginx/conf.d/app.conf` listens on port 80 and is not production TLS configuration. AWS access rules were not available to inspect. |

## API inventory and business data

See the [complete route-pattern inventory](./erp-api-inventory.md): HTTP methods, endpoint pattern, controller/action, and likely consuming module are listed for all routes. Only the health and limited product-catalog routes are intended for the integration flow at this stage. Other business routes retain session/role authentication. The product route's exact request/response contract is below; other routes' parameters and response shapes are described from their current controller code and remain session-authenticated.

Likely data-sharing candidates (not an approved data contract):

| Consumer | Candidate data |
| --- | --- |
| Inventory | Products, facilities/locations, inventory levels/lots/expiry, stock movements, transactions, adjustments, cycle counts. |
| Supply | Catalog/products, suppliers, purchase orders, replenishments, stock movements, receiving/receipts. |
| Accounting | Purchase-order and receipt references, invoices/prepayments, payment terms, currencies, GL account options. Confirm which module owns authoritative financial records and reconciliation. |
| HRM | The code includes user/person endpoints, but not an employee data contract. Require privacy/purpose/field approval before sharing. |
| Payroll | No payroll or compensation integration contract was identified. Do not expose user/person/compensation data absent explicit approval. |

Apply facility scoping and least-privilege access; do not grant other modules blanket access to generic CRUD routes.

## Recommended peer configuration

This module accepts Inventory-to-Supplier calls using the inbound secret below. The remaining variables are suggested for future outbound calls and are not currently wired to a client. Configure values in AWS Secrets Manager or equivalent deployment configuration; never put real values in source control.

```dotenv
INVENTORY_TO_SUPPLY_TOKEN=<secret-from-approved-secret-store>
INVENTORY_API_URL=https://<inventory-host>/<api-context>/api
INVENTORY_API_TOKEN=<secret-from-approved-secret-store>
SUPPLY_API_URL=https://<supply-host>/<api-context>/api
SUPPLY_API_TOKEN=<secret-from-approved-secret-store>
HRM_API_URL=https://<hrm-host>/<api-context>/api
HRM_API_TOKEN=<secret-from-approved-secret-store>
PAYROLL_API_URL=https://<payroll-host>/<api-context>/api
PAYROLL_API_TOKEN=<secret-from-approved-secret-store>
ACCOUNTING_API_URL=https://<accounting-host>/<api-context>/api
ACCOUNTING_API_TOKEN=<secret-from-approved-secret-store>
```

`INVENTORY_TO_SUPPLY_TOKEN` must be a high-entropy secret provisioned to the Supplier app and Inventory caller independently; rotate it in both places. The app rejects requests with no/mismatched token (401) and fails closed with 503 if the expected token is not configured. This token is scoped by code to the read-only product route. The outbound `INVENTORY_API_*` variables are future-facing and unused. Confirm peer context paths; do not assume every server uses `/openboxes`.

## Authentication and headers

The current username/password login remains a browser-session flow. Do not share human credentials or send the browser session cookie as a peer service credential. The new Inventory integration route validates the Bearer token from `INVENTORY_TO_SUPPLY_TOKEN` in constant time and has an explicit controller/action allowlist. It does not authenticate or authorize other routes.

Target request headers after both peers support the agreed scheme:

```http
Authorization: Bearer <service-token>
Accept: application/json
Content-Type: application/json
X-Correlation-ID: <request-id>
```

The Bearer contract is implemented only for `/api/integration/inventory/products`; all existing protected routes continue to expect JSESSIONID/session auth and role checks. Health is intentionally public for load balancer checks.

## Inventory-facing endpoints

The Inventory endpoint supplied is `http://3.1.27.247:8005/api`. It is plain HTTP and the health check identifies it as the Inventory/Laravel service, not this Supplier/OpenBoxes host. TLS negotiation on port 8005 failed. **Do not send a Bearer token to it.** Obtain an HTTPS URL from the Inventory team before making authenticated calls. The Supplier hostname was not provided, so full Supplier URLs below use `<SUPPLIER_HOST>`.

| Purpose | Method and full URL | Auth / headers | Request | Response and success |
| --- | --- | --- | --- | --- |
| Health | `GET https://<SUPPLIER_HOST>/openboxes/api/health` | None; `Accept: application/json` | No body | `200`: `{"service":"OpenBoxes","status":"ok"}` |
| Inventory product catalog (Bearer-enabled) | `GET https://<SUPPLIER_HOST>/openboxes/api/integration/inventory/products` | `Authorization: Bearer <INVENTORY_TO_SUPPLY_TOKEN>`; `Accept: application/json` | No body. Optional query: `q` (max 200 chars), `max` (default 50, capped 100), `offset` (non-negative; default 0). Other query parameters return `400`. Inactive products are excluded. | `200`: `{"data":[{"id":"...","productCode":"...","name":"...","description":null,"unitOfMeasure":"...","lotAndExpiryControl":true,"active":true}],"totalCount":1}`. Fields may be null where the product has no value. |
| Suppliers (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/generic/supplier/` | Existing JSESSIONID; `Accept: application/json` | Generic-resource query parameters; no body | `200`, generic API data wrapper; **session-authenticated, not Bearer-enabled**. |
| Product/supplier links (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/productSuppliers` | Existing JSESSIONID; `Accept: application/json` | Product-supplier filter query fields; no body | `200`: `{"data":[...],"totalCount":n}`; **session-authenticated, not Bearer-enabled**. |
| Purchase orders (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/purchaseOrders` | Existing JSESSIONID; `Accept: application/json` | Existing list filters/pagination; no body | `200`: `{"data":[...],"totalPrice":...,"totalCount":n}`; **session-authenticated, not Bearer-enabled**. |
| Inventory levels (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/facilities/{facilityId}/inventory-levels` | Existing JSESSIONID; `Accept: application/json` | `facilityId` path parameter; no body. Do not request CSV/XLS for the JSON contract. | `200`: `{"data":[...]}`; missing/invalid facility produces a validation/error response; **session-authenticated, not Bearer-enabled**. |
| Stock movements (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/stockMovements` | Existing JSESSIONID; `Accept: application/json` | `direction` required; optional `origin`, `destination`, filters, and pagination; no body | `200`: `{"data":[...],"totalCount":n}`. Missing direction or equal origin/destination returns `400`; **session-authenticated, not Bearer-enabled**. |
| Locations (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/locations` | Existing JSESSIONID and location/user role context; `Accept: application/json` | Existing location filters; no body | `200`: `{"data":[...]}`; **session-authenticated, not Bearer-enabled**. |
| Lot/expiry lookup (existing) | `GET https://<SUPPLIER_HOST>/openboxes/api/products/{productId}/inventoryItems/{lotNumber}` | Existing JSESSIONID; `Accept: application/json` | `productId` and `lotNumber` path parameters; no body | `200`: `{"inventoryItem":{...},"quantityOnHand":n}`; absent lot returns `inventoryItem:null` and quantity 0; **session-authenticated, not Bearer-enabled**. |

Validation/auth errors for the integration product route:

```json
{"errorCode":400,"errorMessage":"Invalid query parameters"}
{"errorCode":401,"errorMessage":"Unauthorized"}
{"errorCode":503,"errorMessage":"Service authentication is not configured"}
{"errorCode":403,"errorMessage":"HTTPS is required"}
```

`400` is returned for unsupported query parameters, invalid `max`/`offset`, or `q` longer than 200 characters. `401` covers missing/malformed/invalid Bearer credentials. `503` means the server-side secret is absent; the response never includes the token. `403` rejects plaintext HTTP (the reverse proxy must overwrite `X-Forwarded-Proto`). No mutation endpoint is exposed under this token.

## Outbound client behavior

Centralize peer URL, credential, timeout, response handling, and safe logging in an integration service/client, reusing or hardening `ApiClientService`. Suggested initial policy:

- Connect timeout: 3 seconds; response timeout: 10 seconds.
- Up to two retries with capped exponential backoff for idempotent GETs and transient network, 429, and 5xx failures.
- Do not retry writes without idempotency keys and receiver-side deduplication.
- Treat 4xx as non-retryable request/auth/contract errors; report 5xx/timeouts explicitly as dependency failures.
- Log peer, route, HTTP status, duration, and correlation ID only. Never log Authorization/cookie headers, tokens, passwords, or sensitive bodies.
- Cache stable reference data only with an agreed short TTL; do not cache inventory quantities, receipts, or financial state by default.

## CORS, HTTPS, and network requirements

- Require HTTPS for all production API traffic. Redirect HTTP to HTTPS or disable public port 80 once certificate renewal requirements are addressed.
- For browser frontends only, allow explicitly approved origins, methods, and headers. Do not use wildcard origins with credentials. Module-to-module requests are not governed by CORS.
- Production Nginx/Apache should terminate TLS, set/overwrite `Host`, `X-Forwarded-For`, and `X-Forwarded-Proto` (use the actual proxy scheme), and proxy only to the private application port. The integration route rejects requests not marked HTTPS; do not trust a client-supplied forwarding header unless the app port is private and the trusted proxy overwrites it. The checked-in Nginx example is not production-ready.
- AWS Security Groups: allow TCP 443 from approved client networks/load balancers; optionally TCP 80 only for redirect/ACME; restrict TCP 22 to administrators. Keep the application port (typically 8080) private and reachable only from the reverse proxy/load balancer. Allow database access only from the application security group. Permit outbound TCP 443 only to approved peer endpoints. Never open database ports between modules.
- Actual DNS names, certificates, frontend origins, peer CIDRs, and server ports were not supplied; infrastructure owners must provide them.

## Health check and examples

`GET /openboxes/api/health` is the repository-default path (live EC2 context path still needs verification). It is anonymous and returns no database status or deployment internals:

```json
{"service":"OpenBoxes","status":"ok"}
```

```sh
curl --fail --silent --show-error \
  https://<SUPPLIER_HOST>/openboxes/api/health
```

An existing business API example uses the documented browser session (not a service-token example):

```sh
curl --fail --silent --show-error \
  --cookie 'JSESSIONID=<session-cookie>' \
  -H 'Accept: application/json' \
  'https://<approved-host>/openboxes/api/products?max=1'
```

For the newly token-protected Inventory endpoint, errors use the JSON bodies listed above. Existing session-protected routes may redirect or use older error envelopes; do not assume they have consistent service-to-service errors.

```json
{"errorCode":401,"errorMessage":"Unauthorized"}
```

The product endpoint does not call another module; the timeout/retry behavior applies to Inventory's HTTP client. Suggested policy: 3-second connect timeout, 10-second response timeout, and at most two retries with capped exponential backoff for GETs only on transient network/429/5xx errors. Do not retry writes without idempotency keys. The existing OpenBoxes outbound client is not hardened for this policy.

## Test plan and deployment commands

Tests added cover Bearer success, missing/invalid token rejection, missing server secret, the public health endpoint, route mapping, and an Inventory-facing product-list request. These are in unit and application integration test sources.

This is not a Composer/Artisan project. `composer install`, Artisan configuration/route commands, and `composer update` are inapplicable. The supplied Gradle wrapper currently points to Gradle 9.7.1, while this legacy Grails project expects Gradle 4.10.3; its wrapper script also lacks the executable bit. Do not alter the user's wrapper change as part of this task. After restoring a compatible, approved build environment:

```sh
bash gradlew test --tests org.pih.warehouse.api.ApiControllerHealthSpec \
  --tests org.pih.warehouse.UrlMappingsSpec --no-daemon
bash gradlew test --no-daemon
bash gradlew assemble --no-daemon
curl --fail --silent --show-error https://<approved-host>/openboxes/api/health
curl --head --fail https://<approved-host>/openboxes/api/health
```

Additionally verify HTTPS certificate validity, CORS from each approved browser origin, AWS Security Group ingress/egress, and Nginx/Apache routing in the deployed environment. The Inventory host probe results above show HTTP-only service on the supplied port; it must be fronted by HTTPS before sending credentials. The Supplier EC2 host and AWS configuration were not provided, so Supplier deployment checks cannot be performed here.
