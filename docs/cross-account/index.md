# Cross-Account Access

How to set up Secrets Manager access when Jenkins is in one AWS account, and your secrets are in another AWS account.

In the example below we assume that Jenkins is running in AWS, using Instance Profiles or some other form of IAM authentication, and that no identity federation is used. Modify this according to your setup.

## AWS Account 111 (Jenkins)

Create the IAM role `jenkins`, for the Jenkins server.

Add permissions to the role that allow Jenkins to assume the `jenkins-secretsmanager` role in account 222:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "sts:AssumeRole",
      "Resource": "arn:aws:iam:222:role/jenkins-secretsmanager"
    }
  ]
}
```

## AWS Account 222 (secrets)

Create the IAM role `jenkins-secretsmanager`.

Add a trust policy to the role that allows Jenkins (from account 111) to assume it:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam:111:role/jenkins"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

Add permissions to the role that allow Jenkins to access Secrets Manager:

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

## Jenkins Configuration

Configure the Jenkins plugin to use IAM AssumeRole authentication. You can either set the `AWS_ROLE_ARN` and `AWS_ROLE_SESSION_NAME` environment variables, or use the plugin's `client` configuration:

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      credentialsProvider:
        assumeRole:
          roleArn: "arn:aws:iam:222:role/jenkins-secretsmanager"
          roleSessionName: "jenkins"
```