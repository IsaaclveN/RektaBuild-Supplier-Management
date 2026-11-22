# OpenBoxes Docker
An overview of how Docker is used within OpenBoxes.

## Docker compose files
We provide the following compose files for running the application under different setups. You can switch between them with the `-f` commandline arg. If you don't specify which file to use, `docker-compose.yml` will be used.

- `docker-compose-base.yml` - Not to be run directly. Contains common configuration that is shared between all compose files.
- `docker-compose.yml` - For running the app with a bundled, containerized database.
- `docker-compose-hostdb.yml` - For running the app against a non-containerized database that is running directly on the host machine
- `docker-compose-remotedb.yml` - For running the app against a database that is externally managed on a separate host

## Running OpenBoxes via Docker (compose)
The following outlines how to run a containerized instance of OpenBoxes.

These compose files support running against officially released OpenBoxes images and locally built ones. See the community guide for [instructions on creating your own Docker image of the application](https://openboxes.gitbook.io/contributor-guide/release-planning/creating-a-new-docker-image).
From the repository root, build the application image from this checkout by preparing the WAR and then building the Compose app service:

```sh
bash ./gradlew prepareDocker -Dgrails.env=prod --console=plain
docker compose -f ./docker/docker-compose.yml build app
```

For HTTPS deployments, include `-f ./docker/docker-compose-https.yml` in Compose commands. Compose uses the image name configured by `OPENBOXES_IMAGE` (or its default) for the locally built image. Re-run the build after changing application source before starting the service.

### Prerequisites
1. Install [Docker](https://docs.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) (we support Docker Engine v24 or later)
2. [An openboxes database with a valid db user](https://openboxes.gitbook.io/contributor-guide/software-development/onboarding/running-openboxes-locally#id-2.-configure-the-database-instance)

### Steps
1. Start Docker
2. Navigate to the `/docker` directory
3. Optionally create a `.env` file containing any environment variable overrides that you need. See `.env.example` for a reference. If you do not provide an override for a variable, the default will be used. For example, given a line like `{DATASOURCE_USERNAME:-openboxes}` in the compose file, if `DATASOURCE_USERNAME` is not defined in your .env file, or it is blank, a value of `openboxes` will be used.
4. Run `docker compose up` to start your containers. You can select the compose file to use by specifying the `-f <config file>` argument. For example: `docker compose -f docker-compose-hostdb.yml up`
5. Look in `docker-compose`'s logs for `Grails application running at http:/localhost:8080/openboxes in environment: production` and connect to that URL

## Production HTTPS

For the Supplier HTTPS deployment, use `docker-compose-https.yml` as an override. The certificate and private key are mounted read-only from `/etc/letsencrypt` on the host; ACME challenge files are served from `/var/www/certbot`. Keep both host paths outside the repository. Do not publish certificates, private keys, or tokens to Git.

The HTTPS override requires Docker Compose v2.24 or later. Run it with `docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml` from this directory. It publishes Nginx on port 443, removes the host-published application port 8080, and replaces the default HTTP-only Nginx configuration. Application traffic should pass through Nginx. The app healthcheck verifies `/openboxes/health` and expects the Spring Boot actuator body `{"status":"UP"}` (note: the separate Grails endpoint `/openboxes/api/health` returns `{"status":"ok"}` — do not point the healthcheck's `UP` matcher at it). Nginx waits for the app healthcheck to pass before starting, and resolves the `app` upstream through Docker's embedded DNS so a recreated app container cannot leave Nginx proxying to a stale IP (502).

Do not source `.env` in a shell. Compose reads it directly. For a deployment from the repository root:

```sh
bash ./gradlew prepareDocker -Dgrails.env=prod --console=plain
cd docker
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml build app
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml up -d --force-recreate app nginx
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml ps
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml exec -T nginx nginx -t
```

Test the app directly, bypassing Nginx, then route the same request through Nginx from the app container. The token stays inside the container environment and is not printed:

```sh
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml exec -T app sh -c 'curl --silent --show-error --include -H "X-Forwarded-Proto: https" -H "Authorization: Bearer $INVENTORY_TO_SUPPLY_TOKEN" -H "Accept: application/json" "http://127.0.0.1:8080/openboxes/api/integration/inventory/products?max=1"'
docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml exec -T app sh -c 'curl --silent --show-error --include --connect-to supplier.petcareplus.tech:443:nginx:443 -H "Authorization: Bearer $INVENTORY_TO_SUPPLY_TOKEN" -H "Accept: application/json" "https://supplier.petcareplus.tech/openboxes/api/integration/inventory/products?max=1"'
curl --silent --show-error --include https://supplier.petcareplus.tech/openboxes/health
```

The HTTPS access log records status and upstream timing/address without query strings or authorization headers. Use `docker compose --env-file ../.env -f docker-compose.yml -f docker-compose-https.yml logs --tail=100 nginx app` to investigate upstream failures.

The application image defaults to the locally built `rektabuild/supplier-openboxes:local`, so Compose never substitutes the upstream `ghcr.io/openboxes/openboxes` image (which does not contain the Supplier ERP integration endpoints). Build it with the commands above before running `up`. Set `OPENBOXES_IMAGE` only when intentionally using a different image, e.g. `OPENBOXES_IMAGE=ghcr.io/openboxes/openboxes:latest`.
