# Troubleshooting

If Secrets Manager secrets do not appear in Jenkins, start here.

Note: You may wish to disable the credentials [cache](../caching/index.md) while debugging.

## Check the Jenkins logs

The plugin uses the AWS Java SDK for its interactions with Secrets Manager. When problems occur, the plugin propagates exceptions from the SDK into the Jenkins logs. These exceptions are often a useful starting point to find out what's wrong.

## Check the Secrets Manager entries

Are the relevant mandatory tags present on the secrets (chiefly `jenkins:credentials:type`)? Without these tags, Jenkins does not know how to present a secret as a Jenkins credential, and will not show it. Consult the README to find which tags to add.

## Check the IAM policy

- Can the Jenkins IAM principal access Secrets Manager? (You can test this independently of Jenkins if you have the AWS CLI available on the Jenkins box by running `aws secretsmanager list-secrets` using the Jenkins IAM principal. This will tell you whether the problem is inside or outside of Jenkins.)
- Is the `secretsmanager:GetSecretValue` permission in a separate IAM statement from the `secretsmanager:ListSecrets` permission? If they are mixed in the same statement object, IAM might silently ignore the whole statement.
