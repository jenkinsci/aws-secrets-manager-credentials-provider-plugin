# SecretSource API

This plugin supports Configuration As Code secret value interpolation through the SecretSource API and resolver.

At interpolation time, the plugin will attempt to retrieve the secret with a name matching the interpolation key in Secrets Manager.

## Usage

AWS CLI:

```bash
aws secretsmanager create-secret --name 'my-password' --secret-string 'abc123' --description 'Jenkins user password'
```

Jenkins CasC:

```yaml
jenkins:
  securityRealm:
    local:
      allowsSignup: false
      users:
      - id: "foo"
        password: "${my-password}"
```

## Error conditions

Secret resolution will fail if any of the following conditions occur:

- The Secrets Manager API call fails.
- No matching secret is found.
- The matching secret has been soft-deleted.
- The matching secret does not contain a string (i.e. it is a binary secret).
