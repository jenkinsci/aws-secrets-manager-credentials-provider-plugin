## Roles

The plugin can access more secrets using secondary IAM roles.

The most common use case is to access secrets in other accounts using [cross-account roles](https://docs.aws.amazon.com/IAM/latest/UserGuide/tutorial_cross-account-with-roles.html). In this setup, Jenkins accesses secrets in its own account using its (implicit) primary role, and is assigned a secondary role for each other account that it should read secrets from.

Secrets in different accounts may have the same name. To allow them to co-exist within Jenkins, credentials from primary and secondary roles use different secret attributes for their IDs:

<table>
    <thead>
        <tr>
            <td>Role</td>
            <td>Credential ID</td>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Primary</td>
            <td>Secret Name</td>
        </tr>
        <tr>
            <td>Secondary</td>
            <td>Secret ARN</td>
        </tr>
    </tbody>
</table>

## Setup

For each secondary role:

1. Create the role and associated policies in AWS.
2. Test that Jenkins can assume the role and retrieve secrets.
3. Add the role ARN to the `roles` list in the plugin configuration.

```yaml
unclassified:
  awsCredentialsProvider:
    beta:
      roles:
        - arn:aws:iam::111111111111:role/foo
        - arn:aws:iam::222222222222:role/bar
```

## Considerations

**Do not add more roles than necessary.** Each additional role necessitates another set of HTTP requests to retrieve secrets. This increases the time to populate the credential list. It also increases the risk of service degradation, as any of those requests could fail.

## Limitations

The primary role cannot currently be turned off. This might be a problem if you use the primary role to access a gateway account, and the secondary roles to access your 'real' accounts, and don't want Jenkins to use Secrets Manager in the gateway account.