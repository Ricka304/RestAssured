# Test Automation Folder Structure

## /src/test/java/tests/

- contract/
  Contains API Contract tests.
  These validate that API responses match the defined JSON Schemas.

- functional/
  Contains Functional tests.
  These validate core business logic, positive/negative flows, boundary conditions,  idempotency and the workflow.
  Aspect	Functional Testing Checks
  Valid Inputs	Returns correct success response
  Invalid Inputs	Returns correct error response
  Boundaries	Works at limits, rejects out-of-range
  Field Rules	Required enforced, optional handled
  Idempotency	Same request → same result
  Full Workflow	Data flows correctly across multiple APIs

- integration/
  Contains Integration tests.
  These verify the behavior of APIs interacting with other services or modules.

- performance/
  Contains performance or load test scripts (if any).

- utils/
  Contains reusable helper classes, test data builders, request/response parsers, and config utilities.

---

## /src/test/resources

- jsonSchemas/
  JSON Schema files used in Contract tests for validating response structures.

- testdata/
  Sample request payloads and expected responses used by tests.

- config/
  Configuration files like test-config.properties (environment URLs, credentials, etc.)
