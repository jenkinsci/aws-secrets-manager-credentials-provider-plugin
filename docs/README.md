# AWS Secrets Manager Credentials Provider

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-secrets-manager-credentials-provider-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/activity/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/aws-secrets-manager-credentials-provider.svg)](https://plugins.jenkins.io/aws-secrets-manager-credentials-provider)

Access credentials from AWS Secrets Manager in your Jenkins jobs.

- [CI Build](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-plugin/)
- [Issues](https://issues.jenkins-ci.org/issues/?jql=component+%3D+aws-secrets-manager-credentials-provider-plugin)

## Features

- Read-only view of Secrets Manager.
- Credential metadata caching (duration: 5 minutes).
- Jenkins [Configuration As Code](https://github.com/jenkinsci/configuration-as-code-plugin) support.

Settings:

- Filters
  - Filter secrets by tag
- Endpoint Configuration
  - Service Endpoint
  - Signing Region

## Setup 

### Jenkins

Install and configure the plugin.

### IAM

Give Jenkins an [IAM policy](iam/index.md) with read access to Secrets Manager.

Required permissions:

- `secretsmanager:GetSecretValue` (resource: `*`)
- `secretsmanager:ListSecrets`

Optional permissions:

- `kms:Decrypt` (if you use a customer-managed KMS key to encrypt the secret)

## Usage

1. **Upload the secret** to Secrets Manager as shown below (see also the [AWS documentation](https://docs.aws.amazon.com/cli/latest/reference/secretsmanager/create-secret.html)).
2. **Reference the secret** by name in your Jenkins job.

A Secrets Manager secret acts as one of the following Jenkins credential types, depending on the data and metadata that you put in it. 

### Secret Text

A simple secret string.

```bash
aws secretsmanager create-secret --name 'newrelic-api-key' --secret-string 'abc123' --description 'Acme Corp Newrelic API key'
```

#### Declarative Pipeline

```groovy
pipeline {
    environment {
        NEWRELIC_API_KEY = credentials('newrelic-api-key')
    }
    stages {
        stage('Foo') {
            echo 'Hello world'
        }
    }
}
```

#### Scripted Pipeline

```groovy
node {
    withCredentials([string(credentialsId: 'newrelic-api-key', variable: 'NEWRELIC_API_KEY')]) {
        echo 'Hello world'
    }
}
```

### Username with Password

A username and password pair.

```bash
aws secretsmanager create-secret --name 'artifactory' --secret-string 'supersecret' --tags 'Key=jenkins:credentials:username,Value=joe' --description 'Acme Corp Artifactory login'
```

#### Declarative Pipeline

```groovy
pipeline {
    environment {
        // Creates variables ARTIFACTORY=joe:supersecret, ARTIFACTORY_USR=joe, ARTIFACTORY_PSW=supersecret
        ARTIFACTORY = credentials('artifactory')
    }
    stages {
        stage('Foo') {
            echo 'Hello world'
        }
    }
}
```

#### Scripted Pipeline

```groovy
node {
    withCredentials([usernamePassword(credentialsId: 'artifactory', usernameVariable: 'ARTIFACTORY_USR', passwordVariable: 'ARTIFACTORY_PSW')]) {
        echo 'Hello world'
    }
}
```

### SSH User Private Key

A private key with a username.

The plugin supports the following private key formats and encoding schemes:

- **Format** 
  - PEM
- **Encoding**
  - PKCS#1 (starts with `-----BEGIN [ALGORITHM] PRIVATE KEY-----`)
  - PKCS#8 (starts with `-----BEGIN PRIVATE KEY-----`)
  - OpenSSH (starts with `-----BEGIN OPENSSH PRIVATE KEY-----`)

```bash
ssh-keygen -t rsa -b 4096 -C 'acme@example.com' -f id_rsa
aws secretsmanager create-secret --name 'ssh-key' --secret-string 'file://id_rsa' --tags 'Key=jenkins:credentials:username,Value=joe' --description 'Acme Corp SSH key'
```

#### Declarative Pipeline

```groovy
pipeline {
    environment {
        // Creates variables KEY=/temp/path/to/key, KEY_USR=joe
        KEY = credentials('ssh-key')
    }
    stages {
        stage('Foo') {
            echo 'Hello world'
        }
    }
}
```

#### Scripted Pipeline

```groovy
node {
    withCredentials([sshUserPrivateKey(credentialsId: 'ssh-key', keyFileVariable: 'KEY', usernameVariable: 'KEY_USR')]) {
        echo 'Hello world'
    }
}
```

### Certificate

A client certificate in PKCS#12 format.

The plugin requires the .p12 file to be encrypted with a zero-length password, as demonstrated below.

```bash
openssl pkcs12 -export -in /path/to/cert.pem -inkey /path/to/key.pem -out certificate.p12 -passout pass:
aws secretsmanager create-secret --name 'code-signing-cert' --secret-binary 'fileb://certificate.p12' --description 'Acme Corp code signing certificate'
```

#### Scripted Pipeline

```groovy
pipeline {
    stages {
        stage('Foo') {
            withCredentials(bindings: [certificate(credentialsId: 'code-signing-cert', keystoreVariable: 'STORE_FILE')]) {
                echo 'Hello world'
            }
        }
    }
}
```

## Configuration

The plugin's default behavior requires **no configuration**.

### Web UI

You can set plugin configuration using the Web UI.

Go to `Manage Jenkins` > `Configure System` > `AWS Secrets Manager Credentials Provider` and change the settings.

### Configuration As Code (CasC)

You can set plugin configuration using Jenkins [Configuration As Code](https://github.com/jenkinsci/configuration-as-code-plugin).

```yaml
unclassified:
  awsCredentialsProvider:
    filters:
      tag:
        key: product
        value: roadrunner
    endpointConfiguration:
      serviceEndpoint: http://localhost:4584
      signingRegion: us-east-1
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

```bash
mvn verify
```

In your IDE:

1. Generate translations: `mvn localizer:generate`. (This is a one-off task. You only need to re-run this if you change the translations, or if you clean the Maven target directory.)
2. Compile.
3. Start Moto: `mvn docker:build docker:start`.
4. Run tests.
5. Stop Moto: `mvn docker:stop`.

## Screenshots

![Credentials screen](img/plugin.png)
