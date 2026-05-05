# SmartCart Documentation

This directory contains the maintained source-of-truth documentation for SmartCart. The goal is to keep these files aligned with the current implementation, runtime behavior, and expected engineering workflows.

## Document set

| File | Purpose |
|---|---|
| `architecture.md` | System context, topology, communication model, and major design decisions |
| `service-catalog.md` | Service-by-service responsibilities, dependencies, and owned data |
| `api-contracts.md` | Current externally relevant HTTP contracts and response behavior |
| `data-model.md` | Current persistence model by service |
| `order-flow.md` | Checkout and cancellation workflow, including Kafka events |
| `security-authentication.md` | JWT, JWKS, gateway security, and service-level security model |
| `operations-runbook.md` | Local development, runtime dependencies, observability, and troubleshooting |
| `smartcart-developer-guide.md` | Developer onboarding, interview narrative, and roadmap-oriented explanation |
| `smartcart-developer-guide.pdf` | Exported PDF version of the developer guide |

## Documentation rules

- These files should reflect the code that exists now.
- Future-state ideas should be marked clearly as planned improvements, not as current behavior.
- Endpoint names, ports, status models, and event names must match the repository.
- When code changes invalidate a document, update the document in the same change set.

## Recommended reading order

1. `architecture.md`
2. `service-catalog.md`
3. `api-contracts.md`
4. `data-model.md`
5. `order-flow.md`
6. `security-authentication.md`
7. `operations-runbook.md`
8. `smartcart-developer-guide.md`
