# Examples: IAM

Typical Secrets Manager IAM policies and customisations for use with this Jenkins plugin.

## Default

The default policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "secretsmanager:GetSecretValue",
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": "secretsmanager:ListSecrets"
        }
    ]
}
```

## Restrict to specific secret

Restrict access to a specific secret like this:

```json
{
    "Effect": "Allow",
    "Action": "secretsmanager:GetSecretValue",
    "Resource": "arn:aws:secretsmanager:<region>:<account-id>:secret:<secret-name>"
}
```

## Restrict to specific secret namespace

Restrict access to a namespace of secrets like this:

```json
{
    "Effect": "Allow",
    "Action": "secretsmanager:GetSecretValue",
    "Resource": "arn:aws:secretsmanager:<region>:<account-id>:secret:<namespace>/*"
}
```