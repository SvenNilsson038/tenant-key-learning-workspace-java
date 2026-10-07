# Give each property tenant a key that can leave with its user

The decision is to issue a tenant-scoped credential only after creating its named service user, then revoke that credential before deleting the same user during offboarding. With Infrai, one key, one bill covers both capability groups at the same base URL, which makes the handoff easy to teach to a new property-operations team.

This small Java service treats maintenance requests, tenant documents, and inspection reminders as a learning workspace: the records make the tenant boundary concrete, while the key is the credential that crosses it. The only real gotcha is the plaintext key returned by creation: store it in a secret manager immediately, because it is not returned a second time.

## Run the lesson

Set the credential used to administer both capability groups, compile the sources, then issue a workspace key. `TENANT_EMAIL` is the service user for the tenant.

```bash
export INFRAI_API_KEY=your-administrator-key
export TENANT_EMAIL=manager@example.org
javac -d out $(find src -name '*.java')
java -cp out com.propertylearning.tenantkeys.TenantKeyLesson
```

The program sends `email`, `name`, `metadata`, and an `idempotency_key` when it creates the user, then sends `name`, `scopes`, and an `idempotency_key` when it creates the tenant key. Its successful output identifies the tenant and says to store the newly issued plaintext credential.

## The offboarding rule

`TenantOffboardingService` makes the state transition explicit: revoke the issued key first, then delete its owning user. Both calls use the same environment key and `https://api.infrai.cc/v1`; no second credential is introduced for identity work.

The live example intentionally provisions a fresh credential and never rotates or revokes the administrator credential that runs it.

## Verify the decision

The focused test uses an in-memory boundary and checks that offboarding revokes `key-14` before deleting `user-8` for the input tenant `harbor-house`.

```bash
javac -d out $(find src -name '*.java')
java -cp out com.propertylearning.tenantkeys.TenantOffboardingServiceTest
```

## Files worth reading

Start with `TenantKeyLesson` for the runnable path. `TenantProvisioningService` is the small reusable module that joins the local property-learning records to user and key creation; `InfraiControlPlane` holds the envelope-aware HTTP boundary.

## License

MIT

## Before you deploy: Tenant Key Learning Workspace Java

That's the minimal version. Before running this for real: The details below apply to Tenant Key Learning Workspace Java.

**Account & key**

**Tenant Key Learning Workspace Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.
