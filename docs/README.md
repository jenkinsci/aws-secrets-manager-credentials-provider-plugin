# AWS Secrets Manager Credentials Provider

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-secrets-manager-credentials-provider-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/activity/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/aws-secrets-manager-credentials-provider.svg)](https://plugins.jenkins.io/aws-secrets-manager-credentials-provider)

Access credentials from AWS Secrets Manager in your Jenkins jobs.

## Contents

- [Authentication](authentication/index.md)
- [Beta Features](beta/index.md)
- [Caching](caching/index.md)
- [Filters](filters/index.md)
- [Networking](networking/index.md)
- [Screenshots](screenshots/index.md)
- Project
  - [Changelog](https://github.com/jenkinsci/aws-secrets-manager-credentials-provider-plugin/releases)
  - [CI Build](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/)
  - [Issues](https://issues.jenkins-ci.org/issues/?jql=component+%3D+aws-secrets-manager-credentials-provider-plugin)

## Features

- Read-only view of Secrets Manager.
- `CredentialsProvider` and `SecretSource` API support.
- Credential metadata caching (duration: 5 minutes).
 
## Setup 

### Jenkins

Install and configure the plugin.

### IAM

Give Jenkins read access to Secrets Manager with an IAM policy.

Required permissions:

- `secretsmanager:GetSecretValue` (resource: `*`)
- `secretsmanager:ListSecrets`

Optional permissions:

- `kms:Decrypt` (if you use a customer-managed KMS key to encrypt the secret)

## Usage

The plugin supports the following secrets resolution APIs:

- [CredentialsProvider](#CredentialsProvider) (high-level API)
- [SecretSource](#SecretSource) (low-level API)

Note: Any string secret is accessible through SecretSource, but only a secret with the `jenkins:credentials:type` tag is accessible through CredentialsProvider. This distinction allows you to share tagged secrets between both APIs, while untagged secrets are only accessible through SecretSource.

### CredentialsProvider

The plugin allows secrets from Secrets Manager to be used as Jenkins credentials.
 
A secret will act as one of the following Jenkins [credential types](https://jenkins.io/doc/pipeline/steps/credentials-binding/), based on the `jenkins:credentials:type` tag that you add to it.

#### Secret Text

A simple text *secret*.

- Value: *secret*
- Tags:
  - `jenkins:credentials:type` = `string`

##### Example

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

#### Username with Password

A *username* and *password* pair.

- Value: *password*
- Tags:
  - `jenkins:credentials:type` = `usernamePassword`
  - `jenkins:credentials:username` = *username*

##### Example

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

#### SSH User Private Key

An SSH *private key*, with a *username*.

- Value: *private key*
- Tags:
  - `jenkins:credentials:type` = `sshUserPrivateKey`
  - `jenkins:credentials:username` = *username*

Common private key formats include PKCS#1 (starts with `-----BEGIN [ALGORITHM] PRIVATE KEY-----`) and PKCS#8 (starts with `-----BEGIN PRIVATE KEY-----`).

##### Example

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

#### Certificate

A client certificate *keystore* in PKCS#12 format, encrypted with a zero-length password.

- Value: *keystore*
- Tags:
  - `jenkins:credentials:type` = `certificate`

##### Example

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

#### Secret File

A secret file with binary *content* and an optional *filename*.

- Value: *content*
- Tags:
  - `jenkins:credentials:type` = `file`
  - `jenkins:credentials:filename` = *filename* (optional)

The credential ID is used as the filename by default. In the rare cases when you need to override this (for example, if the credential ID would be an invalid filename on your filesystem), you can set the `jenkins:credentials:filename` tag.

##### Example

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

## Configuration

Available settings:

- Filter secrets by tag (key, value)
- Endpoint Configuration
  - Service Endpoint
  - Signing Region

The plugin's default behavior requires **no configuration**.

### Web UI

You can set plugin configuration using the Web UI.

Go to `Manage Jenkins` > `Configure System` > `AWS Secrets Manager Credentials Provider` and change the settings.

### Configuration As Code (CasC)

You can set plugin configuration using Jenkins [Configuration As Code](https://github.com/jenkinsci/configuration-as-code-plugin).

```yaml
unclassified:
  awsCredentialsProvider:
    endpointConfiguration:
      serviceEndpoint: http://localhost:4584
      signingRegion: us-east-1
    cacheDuration: 300
    listSecrets:
      filters:
        - key: name
          values:
            - foo
            - bar
      sortOrder: asc
```

## Bugs

All secrets must be uploaded via the AWS CLI or API. This is because the AWS Web console *currently* insists on wrapping your secret string in JSON.

## Development

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

1. Generate translations: `mvn localizer:generate`. (This is a one-off task. You only need to re-run this if you change the translations, or if you clean the Maven `target` directory.)
2. Compile.
3. Start Moto: `mvn docker:build docker:start`.
4. Run tests.
5. Stop Moto: `mvn docker:stop`.
