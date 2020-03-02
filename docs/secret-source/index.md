# SecretSource API

This plugin supports Configuration As Code secret value interpolation through the SecretSource API and resolver.

At interpolation time, the plugin will attempt to retrieve the secret with a name matching the interpolation key in Secrets Manager.

## Usage

```yaml
TODO: add CasC interpolation example
```

## Error conditions

Secret resolution will fail if any of the following conditions occur:

- The Secrets Manager API call fails.
- No matching secret is found.
- The matching secret has been soft-deleted.
