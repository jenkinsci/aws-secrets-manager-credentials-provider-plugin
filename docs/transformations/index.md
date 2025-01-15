# Transformations

Specify how to transform fields from the underlying Secrets Manager secret when presenting the credential.

## Name

Choose how to transform the secret name.

To ensure that your Jenkins configuration is easy to read in the future, You should use the simplest transformer strategy that fits your needs.

:warning: **This feature can break the credentials provider.** If a transformation causes multiple credentials to end up with the same ID, an error occurs. Test your configuration before applying it, and after modifying secrets in Secrets Manager.

### Default

The name is shown unmodified.

### Remove Prefix

The specified prefix is *removed* from the secret name *if present*.

Regex patterns are not supported.

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

## Remove Prefixes

If the secret name starts with one of the specified prefixes, that prefix will be *removed* from the secret name.

If multiple prefixes match the secret name, the most specific (longest) one will be removed. For example, if the prefixes are `foo` and `foo-`, and the secret name is `foo-secret`, then `foo-` will be removed.

Regex patterns are not supported.

This is essentially a more powerful version of the "Remove Prefix" transformation.

#### Example

```yaml
unclassified:
  awsCredentialsProvider:
    transformations:
      name:
        removePrefixes:
          prefixes:
            - "foo-"
            - "bar-"
```

Effects:

- the secret "foo-artifactory" is presented as the credential "artifactory".
- the secret "bar-artifactory" is presented as the credential "artifactory".

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