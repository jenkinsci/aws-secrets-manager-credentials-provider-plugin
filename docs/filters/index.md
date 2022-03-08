# Filters

Specify filters for the `secretsmanager:ListSecrets` API call to control which secrets are shown in Jenkins.

Filters can be specified for the following fields:

- Description (filter key: `description`)
- Name (filter key: `name`)
- Tags (filter keys: `tag-key`, `tag-value`)

You can also specify filters that match when *any* of those fields contain a particular value (filter key: `all`).

Multiple filters combine with an **implicit AND** operator. Multiple values for the same filter combine with an **implicit OR** operator.

Filters are applied server-side by Secrets Manager, and use the Amazon syntax. For full instructions on constructing filters, read the [ListSecrets API documentation](https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_ListSecrets.html).

## Examples

Show credentials with the tag `foo` = [any value]:

```yaml
unclassified:
  awsCredentialsProvider:
    listSecrets:
      filters:
        - key: "tag-key"
          values:
            - "foo"
```

Show credentials with the tag `foo` = `bar` (this involves combining multiple filters):

```yaml
unclassified:
  awsCredentialsProvider:
    listSecrets:
      filters:
        - key: "tag-key"
          values:
            - "foo"
        - key: "tag-value"
          values:
            - "bar"
```

Show credentials where the description contains "foo" or "bar":

```yaml
unclassified:
  awsCredentialsProvider:
    listSecrets:
      filters:
        - key: "description"
          values:
            - "foo"
            - "bar"
```

Show credentials where the name contains "environments/foo" or "environments/bar":

```yaml
unclassified:
  awsCredentialsProvider:
    listSecrets:
      filters:
        - key: "name"
          values:
            - "environments/foo"
            - "environments/bar"
```