# Networking

This plugin uses AWS Secrets Manager, which is a remote service. Follow these recommendations to ensure that it performs well.

## Jenkins outside AWS

- Give Jenkins dedicated network access to Secrets Manager through [AWS PrivateLink](https://aws.amazon.com/privatelink/). (The plugin's performance may vary if it accesses Secrets Manager over the public Internet.)
 
## Jenkins on AWS

- Run Jenkins in an [AWS VPC](https://aws.amazon.com/vpc/).
- Provide a [VPC Endpoint](https://docs.aws.amazon.com/secretsmanager/latest/userguide/vpc-endpoint-overview.html) for Secrets Manager in that VPC.