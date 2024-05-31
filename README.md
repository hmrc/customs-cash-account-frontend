
# customs-cash-account-frontend

A frontend component for the CDS Financials project which aims to allow the user to download and view cash account details 

| Path                                                                   | Description                                                                                       |
| ---------------------------------------------------------------------  | ------------------------------------------------------------------------------------------------- |
| GET  /customs-cash-account-frontend/                                   | Retrieves 6 months cash transactions on the specified CAN                                         |                
| POST /customs-cash-account-frontend/request-cash-transactions          | Retrieves historic cash transactions for a specified date range on the specified CAN              |
| GET  /customs-cash-account-frontend/download-requested-csv             | Download the requested historic cash transactions for a specified date range on the specified CAN |
| GET  /customs-cash-account-frontend/download-csv                       | Download CSV having 6 months cash transactions on the specified CAN                               |


## Running the application locally

The application has the following runtime dependencies:

* `ASSETS_FRONTEND`
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

Once these services are running, you should be able to do `sbt "run 9394"` to start in `DEV` mode or
`sbt "start -Dhttp.port=9394"` to run in `PROD` mode.

The application should be run as part of the CUSTOMS_FINANCIALS_ALL profile due to it being an integral part of service.

## Running tests

There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-2.11/scoverage-report/index.html` in a web browser.

Test coverage threshold is set at 85% - so if you commit any significant amount of implementation code without writing tests, you can expect the build to fail.

## All tests and checks

This is a sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration
tests and produce a coverage report:
> `sbt runAllChecks`
