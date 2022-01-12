# Authentication

Configure the plugin to authenticate with AWS.

The plugin *authenticates* with a single primary AWS account. You then *authorize* it with IAM to access Secrets Manager in one or more accounts.

Authentication methods:

- EC2 Instance Profiles.
- EC2 Container Service credentials.
- Environment variables (set `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `AWS_REGION` before starting Jenkins).
- Java properties (set `aws.accessKeyId`, `aws.secretKey` and `aws.region` before starting Jenkins).
- User profile (configure `~/.aws/credentials` before starting Jenkins).
- Web Identity Token credentials.

Recommendations:

- Use EC2 Instance Profiles when running Jenkins on EC2.
- Only use the long-lived access key methods (environment variables, Java properties, user profile) when there is no other choice. For example, when Jenkins is outside of AWS
- If you see an error along the lines of "Unable to find a region via the region provider chain. Must provide an explicit region in the builder or setup environment to supply a region.", set the region manually with the `AWS_REGION` environment variable.
