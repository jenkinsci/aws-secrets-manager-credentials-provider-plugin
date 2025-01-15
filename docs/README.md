# AWS Secrets Manager Credentials Provider

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-secrets-manager-credentials-provider-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/activity/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/aws-secrets-manager-credentials-provider.svg)](https://plugins.jenkins.io/aws-secrets-manager-credentials-provider)

Access credentials from AWS Secrets Manager in your Jenkins jobs.

This plugin is the high-level counterpart of the [AWS Secrets Manager SecretSource](https://github.com/jenkinsci/aws-secrets-manager-secret-source-plugin) plugin. You can use either plugin individually, or use both of them.

## Contents

- [Beta Features](beta/index.md)
- [Caching](caching/index.md)
- [Client](client/index.md)
- [Cross-Account Access](cross-account/index.md)
- [Filters](filters/index.md)
- [Networking](networking/index.md)
- [Screenshots](screenshots/index.md)
- [Transformations](transformations/index.md)
- [Troubleshooting](troubleshooting/index.md)
- Project
  - [Changelog](https://github.com/jenkinsci/aws-secrets-manager-credentials-provider-plugin/releases)
  - [CI Build](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/)

## Features

- Read-only view of Secrets Manager.
- `CredentialsProvider` API support.
- Credential metadata caching (duration: 5 minutes).
 
## Setup

### IAM

Give Jenkins read access to Secrets Manager with an IAM policy.

Required permissions:

- `secretsmanager:GetSecretValue`
- `secretsmanager:ListSecrets`

Optional permissions:

- `kms:Decrypt` (if you use a customer-managed KMS key to encrypt the secret)

Example:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowJenkinsToGetSecretValues",
            "Effect": "Allow",
            "Action": "secretsmanager:GetSecretValue",
            "Resource": "*"
        },
        {
            "Sid": "AllowJenkinsToListSecrets",
            "Effect": "Allow",
            "Action": "secretsmanager:ListSecrets"
        }
    ]
}
```

### Jenkins

The plugin uses the AWS Java SDK to communicate with Secrets Manager. If you are running Jenkins outside EC2 or EKS you may need to manually configure the SDK to authenticate with AWS. See the [client](client/index.md) configuration guide for more information.

Then, install and [configure](#Configuration) the plugin.

## Usage

The plugin allows secrets from Secrets Manager to be used as Jenkins credentials.

Secrets must conform to the following rules to be usable in Jenkins:

- A secret must have the relevant AWS tags (shown in the sections below) to indicate which Jenkins [credential type](https://jenkins.io/doc/pipeline/steps/credentials-binding/) it is meant to be (e.g. Secret Text, Username With Password). You must add these tags to the secrets. Without these tags, the corresponding credentials will not appear in Jenkins.
- The secret name should conform to Jenkins credential naming rules, as defined in the [credentials-plugin](https://github.com/jenkinsci/credentials-plugin). That is, it should only contain the following characters: `[a-zA-Z0-9_.-]+`. If it contains other characters, you may see undefined behaviour within Jenkins (e.g. URLs containing the credential's ID may not work).

Note: if you have credentials caching enabled, you must wait for the cache to reset before changes to the secrets appear.

### Secret Text

A simple text *secret*.

- Value: *secret*
- Tags:
  - `jenkins:credentials:type` = `string`

#### Example

AWS CLI:

```bash
aws secretsmanager create-secret --name 'newrelic-api-key' --secret-string 'abc123' --tags 'Key=jenkins:credentials:type,Value=string' --description 'Acme Corp Newrelic API key'
```

Declarative Pipeline:

```groovy
pipeline {
    agent any
    environment {
        NEWRELIC_API_KEY = credentials('newrelic-api-key')
    }
    stages {
        stage('Foo') {
            steps {
              echo 'Hello world'
            }
        }
    }
}
```

Scripted Pipeline:

```groovy
node {
    withCredentials([string(credentialsId: 'newrelic-api-key', variable: 'NEWRELIC_API_KEY')]) {
        echo 'Hello world'
    }
}
```

### Username with Password

A *username* and *password* pair.

- Value: *password*
- Tags:
  - `jenkins:credentials:type` = `usernamePassword`
  - `jenkins:credentials:username` = *username*

#### Example

AWS CLI:

```bash
aws secretsmanager create-secret --name 'artifactory' --secret-string 'supersecret' --tags 'Key=jenkins:credentials:type,Value=usernamePassword' 'Key=jenkins:credentials:username,Value=joe' --description 'Acme Corp Artifactory login'
```

Declarative Pipeline:

```groovy
pipeline {
    agent any
    environment {
        // Creates variables ARTIFACTORY=joe:supersecret, ARTIFACTORY_USR=joe, ARTIFACTORY_PSW=supersecret
        ARTIFACTORY = credentials('artifactory')
    }
    stages {
        stage('Foo') {
            steps {
              echo 'Hello world'
            }
        }
    }
}
```

Scripted Pipeline:

```groovy
node {
    withCredentials([usernamePassword(credentialsId: 'artifactory', usernameVariable: 'ARTIFACTORY_USR', passwordVariable: 'ARTIFACTORY_PSW')]) {
        echo 'Hello world'
    }
}
```

### SSH User Private Key

An SSH *Private Key*, with a *Username*.

- Value: *private key*
- Tags:
  - `jenkins:credentials:type` = `sshUserPrivateKey`
  - `jenkins:credentials:username` = *username*

Common private key formats include PKCS#1 (starts with `-----BEGIN [ALGORITHM] PRIVATE KEY-----`) and PKCS#8 (starts with `-----BEGIN PRIVATE KEY-----`).

**Note:** The passphrase field is not supported. (The `SSHUserPrivateKey#getPassphrase()` implementation returns an empty string if called.) This is because any passphrase would have to be stored as a tag on the AWS secret, but tags are non-secret metadata (visible in any `ListSecrets` API call), so the passphrase would offer no meaningful security benefit in this provider.

#### Example

AWS CLI:

```bash
ssh-keygen -t rsa -b 4096 -C 'acme@example.com' -f id_rsa
aws secretsmanager create-secret --name 'ssh-key' --secret-string 'file://id_rsa' --tags 'Key=jenkins:credentials:type,Value=sshUserPrivateKey' 'Key=jenkins:credentials:username,Value=joe' --description 'Acme Corp SSH key'
```

Declarative Pipeline:

```groovy
pipeline {
    agent any
    environment {
        // Creates variables KEY=/temp/path/to/key, KEY_USR=joe
        KEY = credentials('ssh-key')
    }
    stages {
        stage('Foo') {
            steps {
              echo 'Hello world'
            }
        }
    }
}
```

Scripted Pipeline:

```groovy
node {
    withCredentials([sshUserPrivateKey(credentialsId: 'ssh-key', keyFileVariable: 'KEY', usernameVariable: 'KEY_USR')]) {
        echo 'Hello world'
    }
}
```

### Certificate

A client certificate *keystore* in PKCS#12 format, encrypted with a zero-length password.

- Value: *keystore*
- Tags:
  - `jenkins:credentials:type` = `certificate`

#### Example

AWS CLI:

```bash
openssl pkcs12 -export -in /path/to/cert.pem -inkey /path/to/key.pem -out certificate.p12 -passout pass:
aws secretsmanager create-secret --name 'code-signing-cert' --secret-binary 'fileb://certificate.p12' --tags 'Key=jenkins:credentials:type,Value=certificate' --description 'Acme Corp code signing certificate'
```

Scripted Pipeline:

```groovy
node {
    withCredentials([certificate(credentialsId: 'code-signing-cert', keystoreVariable: 'STORE_FILE')]) {
        echo 'Hello world'
    }
}
```

### Secret File

A secret file with binary *content* and an optional *filename*.

- Value: *content*
- Tags:
  - `jenkins:credentials:type` = `file`
  - `jenkins:credentials:filename` = *filename* (optional)

The credential ID is used as the filename by default. In the rare cases when you need to override this (for example, if the credential ID would be an invalid filename on your filesystem), you can set the `jenkins:credentials:filename` tag.

#### Example

AWS CLI:

```bash
echo -n $'\x01\x02\x03' > license.bin
aws secretsmanager create-secret --name 'license-key' --secret-binary 'fileb://license.bin' --tags 'Key=jenkins:credentials:type,Value=file' --description 'License key'
```

Declarative Pipeline:

```groovy
pipeline {
    agent any
    environment {
        LICENSE_KEY_FILE = credentials('license-key')
    }
    stages {
        stage('Example') {
            steps {
              echo 'Hello world'
            }
        }
    }
}
```

Scripted Pipeline:

```groovy
node {
    withCredentials([file(credentialsId: 'license-key', variable: 'LICENSE_KEY_FILE')]) {
        echo 'Hello world'
    }
}
```

#### Github App Credentials (Optional)

Requires *git-source-branch* plugin to create this credential type

A github *private key*, with a *github app id*.

- Value: *content*
- Tags:
  - `jenkins:credentials:type` = `githubApp`
  - `jenkins:credentials:appid` = *Github App Id*

The private key format used is PKCS#8 (starts with `-----BEGIN PRIVATE KEY-----`).

##### Example

AWS CLI:

```bash
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in pkcs1.key -out pkcs8.key
aws secretsmanager create-secret --name 'githubapp' --secret-string 'file://pkcs8.key' --tags 'Key=jenkins:credentials:type,Value=githubApp' 'Key=jenkins:credentials:appid,Value=11111' --description 'Github App Credentials'
```

Declarative Pipeline:

```groovy
pipeline {
    agent any
    environment {
        GITHUB_APP = credentials('githubapp')
    }
    stages {
        stage('Example') {
            steps {
              echo 'Hello world'
            }
        }
    }
}
```

Scripted Pipeline:

```groovy
node {
    withCredentials([usernamePassword(credentialsId: 'githubapp', usernameVariable: 'GITHUBAPP_USR', passwordVariable: 'GITHUBAPP_PSW')]) {
        echo 'Hello world'
    }
}
```

### SecretSource

The plugin allows JCasC to interpolate string secrets from Secrets Manager.

#### Example

AWS CLI:

```bash
aws secretsmanager create-secret --name 'my-password' --secret-string 'abc123' --description 'Jenkins user password'
```

JCasC:

```yaml
jenkins:
  securityRealm:
    local:
      allowsSignup: false
      users:
      - id: "foo"
        password: "${my-password}"
```

## Advanced Usage

You may need to deal with multi-field credentials or vendor-specific credential types that the plugin does not (yet) support.

In this situation you have a couple of choices:

- Use the closest standard multi-field credential (e.g. Username With Password) that fits your requirements.
- Use a string credential, serialize all the fields into the secret value (e.g. as JSON or as a delimited string), and parse them in the job script. (This is a last resort when other methods don't work, e.g. when secret rotation would cause multiple fields to change.)

Example: Jenkins authenticates to Secrets Manager using the primary AWS credential (from the environment). You have a job that performs a particular AWS operation in a different account, which uses a secondary AWS credential. You choose to encode the secondary AWS credential as JSON in the string credential `foo`:

```groovy
node {
    withCredentials([string(credentialsId: 'foo', variable: 'secret')]) {
        script {
            def creds = readJSON text: secret
            env.AWS_ACCESS_KEY_ID = creds['accessKeyId']
            env.AWS_SECRET_ACCESS_KEY = creds['secretAccessKey']
            env.AWS_REGION = 'us-east-1' // or whatever
        }
        sh "aws sts get-caller-identity" // or whatever
    }
}
```

## Configuration

The plugin has a couple of **optional** settings to fine-tune its behavior. **In most installations you do not need to change these settings.** If you need to change the configuration, you can use the Web UI or CasC.

### Web UI

You can set plugin configuration using the Web UI.

Go to `Manage Jenkins` > `Configure System` > `AWS Secrets Manager Credentials Provider` and change the settings.

Available settings:

- [Cache](caching/index.md)
- [Client](client/index.md)
  - CredentialsProvider
  - Endpoint Configuration
  - Region
- ListSecrets configuration
  - [Filters](filters/index.md)
- [Transformations](transformations/index.md)

### Configuration As Code (CasC)

You can set plugin configuration using Jenkins [Configuration As Code](https://github.com/jenkinsci/configuration-as-code-plugin).

**Schema:**

```yaml
unclassified:
  awsCredentialsProvider:
    cache: (boolean)                 # optional
    client:                          # optional
      credentialsProvider: (object)  # optional
      endpointConfiguration:         # optional
        serviceEndpoint: (URL)
        signingRegion: (string)
      region: (string)               # optional
    listSecrets:                     # optional
      filters:
        - key: name
          values:
            - (string)
        - key: tag-key
          values:
            - (string)
        - key: tag-value
          values:
            - (string)
        - key: description
          values:
            - (string)
    transformations:           # optional
      description:
        hide: {}
      name: (object)
```

## Versioning

Version tags for this plugin are of the format:

```
<major>.<autogenerated>
```

For example `1.55.v0fcce24a_9501`.

The `<major>` prefix is incremented to indicate **breaking changes** in the plugin. When this happens, **please read the release notes and test the plugin extra carefully before deploying it to production.** To assist users of the Jenkins Update Center we will also add an `hpi.compatibleSinceVersion` annotation to the POM.

The `<autogenerated>` part is created by the Jenkins [automated plugin release](https://www.jenkins.io/doc/developer/publishing/releasing-cd) system. This is incremented on any non-breaking (minor) change, e.g. new features, bug fixes, or dependency updates. It should normally be safe to adopt these changes straight away.

## Development

### Git

Start by cloning the project.

**Note for Windows users:** some of the file paths in this project may exceed the legacy Win32 path length limit. This may cause an error when cloning the project on Windows. If you see this error, enable Git's Windows longpaths support with `git config --system core.longpaths true` (you might need to run Git as Administrator for this to work). Then try to clone the project again.

### Dependencies

- Docker
- Java
- Maven

### Build 

In Maven:

```shell script
mvn clean verify
```

In your IDE:

1. Generate translations: `mvn localizer:generate`. (This is a one-off task. You only need to re-run this if you change the translations, or if you clean the Maven `target` directory. If the IDE still cannot find the translation symbols after running `mvn localizer:generate`, use a one-off `mvn compile` instead.)
2. Compile.
3. Run tests.

### Run

You can explore how the plugin works by running it locally with [Moto](https://github.com/getmoto/moto) (the AWS mock)...

Start Moto:

```shell
docker run -it -p 5000:5000 motoserver/moto:3.1.18
```

Upload some fake secrets to Moto (like these):

```shell
aws --endpoint-url http://localhost:5000 secretsmanager create-secret --name 'example-api-key' --secret-string '123456' --tags 'Key=jenkins:credentials:type,Value=string' --description 'Example API key'
```

Start Jenkins with the plugin:

```shell
mvn hpi:run
```

Edit the plugin configuration at http://localhost:8080/jenkins/configure to use Moto:

1. Enable the `Endpoint Configuration` option
2. Set `Service Endpoint` to `http://localhost:5000
3. Set `Signing Region` to `us-east-1`
4. Click `Save`
5. Try loading the Jenkins credentials that have come from Moto, or using them in Jenkins jobs.
