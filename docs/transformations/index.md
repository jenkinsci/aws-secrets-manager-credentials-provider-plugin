# Transformations

Specify how to transform fields from the underlying Secrets Manager secret when presenting the credential.

## Name

Choose how to transform the secret name.

### Default

The name is shown unmodified.

### Remove Prefix

The specified prefix is *removed* from the secret name *if present*.

:warning: **This feature can break the credentials provider.** If a transformation causes multiple credentials to end up with the same ID, an error occurs. Test your configuration before applying it, and after modifying secrets in Secrets Manager.

#### Example

```yaml
unclassified:
  awsCredentialsProvider:
    transformations:
      name:
        removePrefix:
          prefix: "foo-"
```

Effects:

- the Secrets Manager secret "foo-artifactory" is presented as the credential "artifactory".

## Description

Choose how to transform the secret description.

### Default

The description is shown unmodified.

### Hide

An empty string will be shown instead of the description.

```yaml
unclassified:
  awsCredentialsProvider:
    transformations:
      description:
        hide: {}
```