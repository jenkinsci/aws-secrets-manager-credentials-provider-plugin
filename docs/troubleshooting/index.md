# Troubleshooting

If Secrets Manager secrets are not appearing in Jenkins, start here.

## Consult the Jenkins logs

The plugin uses the AWS Java SDK for its interactions with Secrets Manager. When problems occur, the plugin propagates exceptions from the SDK into the Jenkins logs. These exceptions are often a useful starting point to find out what's wrong.
