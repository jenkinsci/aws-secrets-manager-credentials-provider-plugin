# Client

The plugin allows you to configure the Secrets Manager client that it uses to access secrets.

**We recommend that you use the defaults whenever possible.** This will allow Jenkins to inherit AWS configuration from the environment. Only set these client options if you really need to (for example you have multiple Jenkins AWS plugins installed, and need the Secrets Manager plugin to behave differently to the others).

## Credentials Provider

The plugin supports the following `AWSCredentialsProvider` implementations to authenticate and authorize with Secrets Manager.

*Note: This is not the same thing as a Jenkins `CredentialsProvider`.*

Recommendations:

- Use EC2 Instance Profiles when running Jenkins on EC2.
- Only use the long-lived access key methods when there is no other choice. For example, when Jenkins is running outside of AWS.
- If you use the AWS Secrets Manager CredentialsProvider and SecretSource plugins together, you SHOULD use the Default strategy. This allows both plugins to transparently pick up the same authentication information.
- If you see an error along the lines of "Unable to find a region via the region provider chain. Must provide an explicit region in the builder or setup environment to supply a region.", set the region manually.

**Authorization note:** IAM is always present, no matter which authentication mechanism you use. This is because, even if Jenkins is running outside AWS and you use an AWS keypair, the keypair belongs to an IAM user, and AWS must still check that the IAM user is allowed to access Secrets Manager.

### Default

This uses the standard AWS credentials lookup chain.

The authentication methods in the chain are:

- EC2 Instance Profiles.
- EC2 Container Service credentials.
- Environment variables (set `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_REGION` before starting Jenkins).
- Java properties (set `aws.accessKeyId`, `aws.secretKey`, and `aws.region` before starting Jenkins).
- User profile (configure `~/.aws/credentials` before starting Jenkins).
- Web Identity Token credentials.

### Profile

This allows you to use named AWS profiles from `~/.aws/config`.

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      credentialsProvider:
        profile:
          profileName: "foobar"
```

### STS AssumeRole

This allows you to specify IAM roles inline within Jenkins.

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      credentialsProvider:
        assumeRole:
          roleArn: "arn:aws:iam::111111111111:role/foo"
          roleSessionName: "jenkins"
```

### Static Key Pair

This allows you to specify a static long-lived AWS keypair within Jenkins.

The `secretKey` value will be stored in Jenkins' plugin XML configuration, encrypted using `hudson.util.Secret`. This provides a modicum of security, but not much.

If you use this authentication strategy together with Jenkins CasC, you SHOULD inject the keypair (or at least the `secretKey`) via CasC secret interpolation. This is to avoid hardcoding the secret key in plain text within your casc.yaml.

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      credentialsProvider:
        static:
          accessKey: "${aws-access-key}"    # e.g. AKIAIOSFODNN7EXAMPLE
          secretKey: "${aws-secret-key}"    # e.g. wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

## Endpoint Configuration

You can set the AWS endpoint configuration for the client.

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      endpointConfiguration:
        serviceEndpoint: "http://localhost:4584"
        signingRegion: "us-east-1"
```

## Region

You can set the AWS region for the client.

```yaml
unclassified:
  awsCredentialsProvider:
    client:
      region: "us-east-1"
```
