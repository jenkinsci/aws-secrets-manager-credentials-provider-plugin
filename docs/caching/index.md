# Caching

This plugin uses AWS Secrets Manager, which is a remote service. It uses caching where appropriate to improve reliability and responsiveness.

## Credentials List

The plugin caches the credential list that it obtains from Secrets Manager by default. This list contains metadata about the secrets (like names and descriptions), **not** the secret values.

When the cache is enabled, credentials are cached for 5 minutes.

You can disable the cache by setting the relevant configuration option in CasC or the Web UI:

```yaml
unclassified:
  awsCredentialsProvider:
    cache: false
``` 
 
If Jenkins is running, this change will take effect after the current cache expires.

This change has consequences:

- Disabling the cache increases load on the AWS <code>secretsmanager:ListSecrets</code> endpoint. In high load situations, Jenkins may exceed the endpoint's [rate limit](https://docs.aws.amazon.com/secretsmanager/latest/userguide/reference_limits.html).
- Disabling the cache increases the time required to look up credentials. (Every lookup will require network round-trips, which take time.)

As such, you should generally avoid disabling the cache, except for testing purposes.

## Credential Value

The plugin never caches the secret value of a credential in normal use. The value is retrieved from Secrets Manager at binding time.
 
Exceptions:
 
- Snapshots. A downstream credential consumer may explicitly request a snapshot of a credential by calling `CredentialsProvider.snapshot(credential)`. The snapshot process runs on the Jenkins master. It returns a clone of the credential, with an in-memory copy of the secret value.