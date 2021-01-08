# Fields

Specify how fields from the underlying Secrets Manager secret are presented in Jenkins.

## Name

Choose how to present and resolve the secret name.

### Default

`CredentialsProvider`: The name is presented without transformation.

`SecretSource`: The name is resolved without transformation.

### Remove Prefix

:warning: **This feature can break the credentials provider.** If a transformation causes multiple credentials to end up with the same ID, an error occurs. Test your configuration before applying it, and after modifying secrets in Secrets Manager.

`CredentialsProvider`: The specified prefix is *removed* from the secret name *if present*.

`SecretSource`: The specified prefix is *added* to the CasC secret name when resolving the secret.

#### Example

```yaml
unclassified:
  awsCredentialsProvider:
    fields:
      name:
        removePrefix:
          prefix: "foo-"
```

Effects:

- `CredentialsProvider`: the Secrets Manager secret "foo-artifactory" is presented as the credential "artifactory".
- `SecretSource`: the CasC secret "artifactory" resolves to the Secrets Manager secret "foo-artifactory".

## Description

`CredentialsProvider`: Choose whether to show or hide the secret description.

`SecretSource`: No effect.

### Show (default)

The description is shown.

### Hide

An empty string will be shown instead of the description.

```yaml
unclassified:
  awsCredentialsProvider:
    fields:
      description: false
```