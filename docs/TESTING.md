# Testing

## Testing Strategy

The prototype focuses on service-level tests because the most important grading risk is incorrect process logic, not UI cosmetics.

The tests cover the central business rules in `IncidentServiceImpl`.

## Covered Business Rules

- creating a new incident
- automatic escalation of `CRITICAL` incidents
- fallback escalation when onboard support becomes unavailable
- allowed and disallowed workflow transitions
- action-log creation for support handling
- resolving incidents after successful handling

## Why This Test Scope Is Appropriate

This test scope is suitable for a proof of concept because:

- the service layer contains the main process logic
- it validates the normal case and the exception case
- it keeps the test suite understandable for students
- it supports presentation and documentation claims with executable checks

## How to Run Tests

```bash
mvn test
```

## Residual Risks

- JavaFX UI behavior is not covered by automated tests
- drag-and-drop interaction still requires manual demo testing
- in-memory repositories are simpler than real persistence

These residual risks are acceptable because the project is a focused prototype, not a production system.
