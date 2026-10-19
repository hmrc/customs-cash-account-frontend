
# customs-cash-account-frontend

## About
A frontend microservice for the CDS Financials project which aims to allow the user to download and view cash account details

| Key                             | Value                                                                                                   | 
|---------------------------------|---------------------------------------------------------------------------------------------------------|
| Digital service                 | CDS Financials                                                                                             |
| Local port                      | 9394                                                                                            |
| Base path                       | /customs/cash-account                                                                             |
| Back-end                        | [customs-financials-api](https://github.com/hmrc/customs-financials-api) (port `6792`)                |
| Acceptance tests                | [customs-cash-account-acceptance-tests](https://github.com/hmrc/customs-cash-account-acceptance-tests)                      |
| Performance tests               | [customs-cash-account-performance-tests](https://github.com/hmrc/customs-cash-account-performance-tests)|
| Stubs used in Local and Staging | [customs-financials-hods-stub](https://github.com/hmrc/customs-financials-hods-stub), [customs-financials-sdes-stub](https://github.com/hmrc/customs-financials-sdes-stub) |

## How to Run this Service

### Prerequisites

This service depends on:
* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `BAS_GATEWAY`
* `CA_FRONTEND`
* `SSO`
* `USER_DETAILS`
* `CUSTOMS_FINANCIALS_API`
* `CUSTOMS_FINANCIALS_HODS_STUB`
* `CUSTOMS_FINANCIALS_SDES_STUB`
* `CONTACT_FRONTEND`

Use [Service Manager](https://github.com/hmrc/customs-declare-exports-frontend#service-manager-profiles) to start all the services that CDS Exports depends on:

```bash
sm2 --start CUSTOMS_FINANCIALS_ALL
```

### Running the Service Locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt:
```bash
sm2 --stop CUSTOMS_CASH_ACCOUNT_FRONTEND
sbt run
```

The service starts on port `9394`. To access it, [sign in through the auth login stub](https://github.com/hmrc/exports-ui-acceptance-tests#enrolment-required), and you will be redirected to http://localhost:9876/customs/payment-records.

## How to Test This Service

### Local

#### Unit Tests
```bash
sbt test
```

There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-3.x.x/scoverage-report/index.html` in a web browser.

The minimum requirement for test coverage is 90%. Builds will fail when the project drops below this threshold.


#### Acceptance Tests (Smoke and Regression)
The acceptance tests for this service are in [customs-cash-account-acceptance-tests](https://github.com/hmrc/customs-cash-account-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/customs-cash-account-acceptance-tests#running-the-automation-tests).

#### Performance Tests
The performance tests for this service are in [customs-cash-account-performance-tests](https://github.com/hmrc/customs-cash-account-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/exports-declarations-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-cash-account-frontend/), manual testing is performed in the QA and/or Staging environments. For more details see [Manual Service Verification in QA and Staging](https://github.com/hmrc/customs-cash-account-acceptance-tests#run-e2e-smoke-test-via-jenkins-pipeline).

## Service Catalogue
- [customs-cash-account-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-cash-account-frontend)

## Jenkins Pipeline
- Front End Build and Deployment Pipeline
 -  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-cash-account-frontend/)
 -  [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-cash-account-frontend-pipeline/)
The acceptance test Jenkins jobs (smoke and regression) are listed in the customs-cash-account-acceptance-tests [README](https://github.com/hmrc/customs-cash-account-acceptance-tests#customs-cash-account-acceptance-tests).
The performance test Jenkins jobs are listed in the customs-cash-account-performance-tests [README](https://github.com/hmrc/customs-cash-account-performance-tests#customs-cash-account-performance-tests).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile                            | Use                                         | Services started                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|------------------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CUSTOMS_FINANCIALS_FRONTEND_DEPS` | Running and developing this service locally | "CUSTOMS_MANAGE_AUTHORITIES_FRONTEND", "CUSTOMS_CASH_ACCOUNT_FRONTEND", "CUSTOMS_GUARANTEE_ACCOUNT_FRONTEND", "CUSTOMS_FINANCIALS_API", "CUSTOMS_FINANCIALS_EMAIL_THROTTLER", "ASSETS_FRONTEND_2", "AUTH", "AUTH_LOGIN_STUB", "AUTH_LOGIN_API", "CENTRALISED_AUTHORISATION_SERVER", "SSO", "USER_DETAILS", "CUSTOMS_FINANCIALS_HODS_STUB", "CUSTOMS_FINANCIALS_SDES_STUB", "IDENTITY_VERIFICATION", "BAS_STUBS", "BAS_GATEWAY", "BAS_PROXY", "BAS_GATEWAY_FRONTEND", "BAS_STUB_FRONTEND", "TWO_STEP_VERIFICATION_RULE_ENGINE", "TAR_FRONTEND", "DATASTREAM", "EMAIL", "HMRC_EMAIL_RENDERER", "CUSTOMS_DATA_STORE", "CUSTOMS_FINANCIALS_SESSION_CACHE", "TRACKING_CONSENT_FRONTEND", "CONTACT_FRONTEND", "ACCESSIBILITY_STATEMENT_FRONTEND"  |
| `CUSTOMS_FINANCIALS_ATS`           | Running the CDS Financials acceptance tests | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `CUSTOMS_FINANCIALS_TESTS`         | Running the CDS Financials tests            | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `CUSTOMS_CASH_ACCOUNT_ATS`         | Running the CDS Financials acceptance tests | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `CUSTOMS_FINANCIALS_ALL`           | All CDS Financials services                 | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

| Service                           | Port | Repository                                                                     |
|-----------------------------------|------|--------------------------------------------------------------------------------|
| `CUSTOMS_CASH_ACCOUNT_FRONTEND`   | 9394 | This service                                                                   |


## Endpoints

All paths are relative to `/customs/cash-account` and need an [authenticated user with the HMRC-CUS-ORG enrolment](https://github.com/hmrc/exports-ui-acceptance-tests#enrolment-required).

You can find a list of microservice specific routes here - `/conf/app.routes`

| Method | Path                                    | Purpose                                                                                           | Sample request (query / form body)                                                   | Response                                                                                               |
|--------|-----------------------------------------|---------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| GET    | `/`                                     | Service start, show account detail page, redirect to v2 if enabled                                | page: Int (optional)                                                                 | `200`, `303` → `/v2` (if v2 enabled)                                                                   |
| GET    | `/v2`                                   | Show account detail page v2                                                                       | page: Int (optional)                                                                 | `200`                                                                                                  |
| POST   | `/v2`                                   | Submit form to search by MRN, UCR or exact payment                                                | form: SearchTransactionsFormProvider, page: Int (optional)                           | `303` → `/transaction-search/:searchInput`, `/account-unavailable`, `400`, `404`                       |
| GET    | `/too-many-transactions`                | Display cash account exceeded threshold page                                                      | –                                                                                    | `200`                                                                                                  |
| GET    | `/v2/too-many-transactions`             | Display cash account exceeded threshold page                                                      | –                                                                                    | `200`                                                                                                  |
| GET    | `/request-cash-transactions `           | Show date form for request cash transactions page                                                 | –                                                                                    | `200`                                                                                                  |
| POST   | `/request-cash-transactions `           | Validate requested date range, then redirect to results page                                      | form: CashTransactionDates                                                           | `303` → `/requested-cash-transactions`                                                                 |
| GET    | `/requested-cash-transactions  `        | Retrieve and display cash transactions for the cached date range                                  | –                                                                                    | `303` -> `/request-cash-transactions`, `/too-many-requested-cash-transactions`, `/account-unavailable` |
| GET    | `/too-many-requested-cash-transactions` | Display too many transactions found page                                                          | range: models.RequestedDateRange                                                     | `200`                                                                                                  |
| GET    | `/verify-your-email `                   | Retrieve the user's unverified email address and display the email verification page              | –                                                                                    | `200`                                                                                                  |
| GET    | `/undeliverable-email `                 | Retrieve the user's verified email address and display an email undeliverable page                | –                                                                                    | `200`                                                                                                  |
| GET    | `/request-cash-transactions/v2 `        | Display the cash transaction date request form                                                    | –                                                                                    | `200`                                                                                                  |
| POST   | `/request-cash-transactions/v2 `        | Retrieves historic cash transactions for a specified date range on the specified CAN              | form: CashTransactionDates                                                           | `303` -> `/request-cash-transactions`                                                                  | 
| GET    | `/selected-cash-transactions`           | Display selected transaction request details using cached dates and account information           | –                                                                                    | `200` `404` `303` -> `/request-cash-transactions/v2`, `/account-unavailable`                           |
| POST   | `/selected-cash-transactions`           | Submit a cash account statement request for the selected date range                               | –                                                                                    | `303` -> `/requested-duplicate-dates`, `/selected-confirmation`, `/requested-too-many-transactions`    |
| GET    | `/requested-too-many-transactions`      | Display too many statement requests page                                                          | –                                                                                    | `200` `303` -> `/`                                                                                     |
| GET    | `/requested-duplicate-dates`            | Display duplicate statement request page                                                          | displayMsg: String, startDate: String, endDate: String                               | `200`                                                                                                  |
| GET    | `/too-many-selected-transactions`       | Display too many transactions selected page                                                       | range: models.RequestedDateRange                                                     | `200`                                                                                                  |
| GET    | `/selected-confirmation`                | Display confirmation page using cached dates and user email                                       | –                                                                                    | `200`, `303` -> `/account-unavailable `                                                                |
| GET    | `/account-unavailable`                  | Show account unavailable page                                                                     | –                                                                                    | `200`                                                                                                  |
| GET    | `/v2/account-unavailable`               | Show account unavailable page v2                                                                  | –                                                                                    | `200`                                                                                                  |
| GET    | `/not-subscribed-for-cds`               | Show unauthorised page                                                                            | –                                                                                    | `200`                                                                                                  |
| GET    | `/logout-survey`                        | Logout with survey                                                                                | -                                                                                    | `303` -> `signOutUrl?continue=<feedbackService>  `                                                     |
| GET    | `/logout `                              | Logout without survey                                                                             | –                                                                                    | `303` -> `signOutUrl`                                                                                  |
| GET    | `/download-requested-csv`               | Download the requested historic cash transactions for a specified date range on the specified CAN | disposition: String (optional), dateRange: RequestedDateRange                        | `200` `303` -> `/` `/csv-download-error`                                                               |
| GET    | `/download-csv`                         | Download CSV having 6 months cash transactions on the specified CAN                               | disposition: String (optional)                                                       | `200` `303` -> `/` `/csv-download-error`                                                               |
| GET    | `/csv-download-error`                   | Unable to download CSV                                                                            | –                                                                                    | `200`                                                                                                  |
| GET    | `/transaction/:ref`                     | Display details for a specific cash account transaction if available                              | ref: String, page: Int (optional) eg./transaction/<ref>?page=<int>                   | `200` `404`                                                                                            |
| GET    | `/transaction-search/:searchInput`      | Display declaration search results for a specific cash account if available                       | page: Int, searchInput: String  eg./transaction-search/<searchInput>?page=<int>      | `200` `404` `303` -> `/search/<searchInput>`                                                           |
| GET    | `/search/:searchValue `                 | Display payment search results for a specific cash account if available                           | searchValue: String, page: Int  eg./search/<searchValue>?page=<int>                  | `200` `404`                                                                                            |

## Enrolment Required

The service can be accessed via MIDVA home page (CUSTOMS FINANCIALS FRONTEND microservice) using 'View cash account' link under 'Cash account'

Access CUSTOMS FINANCIALS FRONTEND using below enrolments and with below sample EORI numbers
via http://localhost:9949/auth-login-stub/gg-sign-in (on local) or https://<host:port>/auth-login-stub/gg-sign-in on DEV/QA/STAGING

Redirect URL - `/customs/cash-account `

| Enrolment Key	 | Identifier Name | Identifier Value |
|----------------|-----------------|------------------|
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982000` |
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982001` |

## Developer Notes

### Feature Flags

Feature flags are used appropriately on different envs and could be updated in customs-cash-account-frontend.yaml on pertinent env

### Enable features
| Command                                       | Description                                        |
|-----------------------------------------------|----------------------------------------------------|
| `sbt "run -Dfeatures.some-feature-name=true"` | enables a feature locally without risking exposure |

Feature switches can be enabled per-environment via the `app-config-<env>` project in customs-cash-account-frontend.conf/.yaml

### Available feature flags
| Flag                      | Description                                                                                                                                         |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `fixed-system-time`       | Enable the fixed datetime for DateTimeService by enabling the stub data for current and requested statements. It is only enabled in Development env |
| `transactions-timeout`    | Enable the transaction timeout                                                                                                                      |
| `cash-account-v2-enabled` | Enable the new cash account design                                                                                                                  |

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

### Scalastyle checks
| Command               | Description                                                                |
|-----------------------|----------------------------------------------------------------------------|
| `sbt scalastyle`      | Runs scala style checks based on scalastyle-config.xml                     |                                                     |
| `sbt Test/scalastyle` | Runs scala style checks for unit tests based on test-scalastyle-config.xml |


### Run all checks
This is an sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`
