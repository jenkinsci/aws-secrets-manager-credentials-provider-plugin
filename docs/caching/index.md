# Caching

This plugin uses AWS Secrets Manager, which is a remote service. It uses caching where appropriate to improve reliability and responsiveness.

## Credentials List

The plugin caches the credential list that it obtains from Secrets Manager. The default cache duration is 5 minutes (300 seconds). You can configure this value.

## Credential Value

The plugin never caches the secret value of a credential in normal use. The value is retrieved from Secrets Manager at binding time.
 
Exceptions:
 
- Snapshots. A downstream credential consumer may explicitly request a snapshot of a credential by calling `CredentialsProvider.snapshot(credential)`. The snapshot process runs on the Jenkins master. It returns a clone of the credential, with a local in-memory copy of the secret value.