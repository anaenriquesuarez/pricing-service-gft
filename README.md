# Pricing Service - GFT technical assessment

Spring Boot **4.0.10**, Java **17**, Spring MVC, Spring Data JPA and H2. A small product-pricing REST API built using ports-and-adapters (hexagonal architecture).

## Quick start

Requirements: **JDK 17+**, internet access on first build. Open this folder (the one containing `pom.xml`) as a Maven project in IntelliJ IDEA and select **JDK 17** for the project and Maven runner.

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`. The tests run without a separately installed H2 instance. If the wrapper script needs permission: `chmod +x mvnw`.

- API: `http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API contract: `http://localhost:8080/openapi.yaml` (maintained source: `src/main/resources/static/openapi.yaml`)
- H2 console (development only): `http://localhost:8080/h2-console`
- H2 JDBC URL: `jdbc:h2:mem:pricingdb`, user `sa`, empty password.

The expected example response contains `priceList: 2` and `price: 25.45`.

## API

`GET /api/v1/prices` requires `applicationDate` (ISO local date-time), `productId` (positive integer), and `brandId` (positive integer).

- **200** returns product ID, brand ID, applicable price list, validity dates, final price and currency.
- **400** for malformed or invalid request parameters.
- **404** when no applicable tariff is found.

Date validity bounds are inclusive. If more than one tariff applies, the highest numerical priority wins; for equal priorities, the largest internal database ID breaks ties deterministically (a documented implementation choice).

## Hexagonal architecture

- `domain/model/Price`: immutable model (`record`), no ORM annotations.
- `domain/port/PriceRepositoryPort`: outbound persistence contract.
- `domain/exception/PriceNotFoundException`: use-case error.
- `application/usecase/GetApplicablePriceUseCase`: inbound application contract.
- `application/service/GetApplicablePriceService`: calls the outbound port, independent of JPA.
- `infrastructure/adapter/in/rest`: HTTP controller and response DTO.
- `infrastructure/adapter/out/persistence`: JPA entity, query, mapper and adapter.
- `infrastructure/exception/GlobalExceptionHandler`: translates application/validation failures to HTTP.

The JPA query selects only the relevant brand/product and dates, orders by priority and ID and applies a one-row page size in SQL. H2 data is initialized from `data.sql` at application startup.

## Testing

`./mvnw clean verify` runs:

- **2 unit tests** (JUnit + Mockito, service isolated from Spring and database).
- **15 MockMvc integration tests** (API, validation, error handling and boundary cases).
- **5 E2E HTTP tests** (REST Assured against a randomly bound embedded server).
- **1 Spring context test**.

The five required example time cases are included in both the integration and HTTP E2E suites.

## Spring Boot 4 migration

- Uses `spring-boot-starter-webmvc` rather than the old `spring-boot-starter-web`.
- Uses `spring-boot-starter-webmvc-test` and the new MockMvc test annotation package.
- Uses Springdoc **3.2.1** (Jackson 3-compatible release).
- Uses the dedicated `spring-boot-h2console` module.
- Removes Lombok and annotation-processor dependencies; JPA entity getters are explicit.
- Uses Spring MVC method-parameter validation and a dedicated exception mapper.
- Dependency versions are managed by the Spring Boot BOM wherever possible.

The OpenAPI YAML is the maintained documentation contract, but Java code is **not generated** from it. This is a documented contract-first *artifact*, not strict generator-based API First.

## Delivery checklist

Run `./mvnw clean verify`, exercise the sample URL, verify Swagger UI loads the contract, and inspect IntelliJ's dependency vulnerability report before submitting. Keep `target/` and `.idea/` out of version control. Any current alerts require separate verification; upgrading major versions does not itself prove the dependency tree is free of vulnerabilities.
