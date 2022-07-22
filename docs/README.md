# AWS Secrets Manager Credentials Provider

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-secrets-manager-credentials-provider-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/activity/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/aws-secrets-manager-credentials-provider.svg)](https://plugins.jenkins.io/aws-secrets-manager-credentials-provider)

Access credentials from AWS Secrets Manager in your Jenkins jobs.

This plugin is the high-level counterpart of the [AWS Secrets Manager SecretSource](https://github.com/jenkinsci/aws-secrets-manager-secret-source-plugin) plugin. You can use either plugin individually, or use both of them.

## Contents

- [Beta Features](beta/index.md)
- [Caching](caching/index.md)
- [Client](client/index.md)
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
 
Jenkins must know which [credential type](https://jenkins.io/doc/pipeline/steps/credentials-binding/) a secret is meant to be (e.g. Secret Text, Username With Password), in order to present it as a credential. To do this, **you MUST add the relevant AWS tags to the secrets in Secrets Manager**, as shown in the sections below. (If the credentials cache is enabled you must also wait for that to refresh before the newly annotated secrets appear in Jenkins.) Without these tags, the corresponding credentials will not appear in Jenkins.

**Note:** If you use this plugin together with the AWS Secrets Manager SecretSource plugin, remember that any string secret is accessible through SecretSource, but only a secret with the `jenkins:credentials:type` tag is accessible through CredentialsProvider. This distinction allows you to share tagged secrets between both APIs, while untagged secrets are only accessible through SecretSource.

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

An SSH *private key*, with a *username*.

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
      name:
        removePrefix:
          prefix: (string)
```

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
