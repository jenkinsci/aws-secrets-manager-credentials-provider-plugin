# Filters

The CredentialsProvider implementation in this plugin calls `secretsmanager:ListSecrets` to cache the secrets' metadata. At this time, Secrets Manager does not support server-side restrictions on this list, so it returns all secrets in the AWS account, whether you have given Jenkins the `secretsmanager:GetSecretValue` permission to actually resolve those secrets or not. This can result in unwanted entries appearing in the credentials UI, which users will mistake for resolvable credentials. 

To improve the user experience of this aspect of Jenkins, you can specify optional filters in the plugin configuration. Only the secrets that match the filter criteria will be presented through the CredentialsProvider. This hides unwanted entries from the credentials UI. 

Notes:

- These are client-side filters. As such they only provide usability benefits. They have no security benefits, as Jenkins still fetches the full secret list from AWS.
- The SecretSource implementation does not use the filters, as they are not relevant to it.

## Tag Filter

You can choose to only show credentials that have a tag with a particular key and value.

Example: `product` = `foo`.

```yaml
unclassified:
  awsCredentialsProvider:
    filters:
      tag:
        key: product
        value: foo
```