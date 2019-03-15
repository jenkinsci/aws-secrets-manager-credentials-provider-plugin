package io.jenkins.plugins.aws_secrets_manager_credentials_provider.fixtures;

import java.util.Collections;
import java.util.Map;

public class AwsSecret {
    private final String name;
    private final String description;
    private final Map<String, String> tags;
    private final String value;

    private AwsSecret(String name, String description, Map<String, String> tags, String value) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.value = value;
    }

    public static final AwsSecret TEXT = new AwsSecret("text", "", Collections.emptyMap(), "supersecret");

    public static final AwsSecret PRIVATE_KEY = new AwsSecret("private-key", "", Collections.emptyMap(), String.join("\n"
            , "-----BEGIN RSA PRIVATE KEY-----"
            , "MIIEowIBAAKCAQEAngWMYnda9vD2utvbAdgCOLVNanA/MW50er5ROW21it/eph1u"
            , "6RCuZ0CiuYUE5Eb8kOOQP7MTL3Ixyv9GW6hmMZwjyvcCamKj7cYuEHBYkn0X2Jgw"
            , "syPGUWZwITgSxgb/VfjRKbAtUdvXNFjHxknUlaVd+G6gQpN5Lv3//O/EglmVqf1d"
            , "CM2xAy9Ixk9roMSmBpgwC7lCsi1W9IGdLrjLAC96BrJkHX1EDQDdB8tWg8qLjZfr"
            , "L1ioddG/NDH8lOUetWX9SB5WF4xi/oBRNvSCwmBAa8v2DvhS/TEwcWAsReclRCNW"
            , "5eGAqhbb0Kl8E0hYJdFlEKYjQH3y5cZtqMAiuwIDAQABAoIBAGQK2TThoYpjRaFJ"
            , "XZ8ONWHXjpqLU8akykOHR/8WsO+qCdibG8OcFv4xkpPnXhBzzKSiHYnmgofwQQvm"
            , "j5GpzIEt/A8cUMAvkN8RL8qihcDAR5+Nwo83X+/a7bRqPqB2f6LbMvi0nAyOJPH0"
            , "Hw4vYdIX7qVAzF855GfW0QE+fueSdtgWviJM8gZHdhCqe/zqYm016zNaavap530r"
            , "tJ/+vhUW8WYqJqBW8+58laW5vTBusNsVjeL40yJF8X/XQQcdZ4XmthNcegx79oim"
            , "j9ELzX0ttchiwAe/trLxTkdWb4rEFz+U50iAOMUdS8T0brb5bxhqNM/ByiqQ28W9"
            , "2NJCVEkCgYEA0phCE9iKVWNZnvWX6+fHgr2NO2ShPexPeRfFxr0ugXGTQvyT0HnM"
            , "/Q//V+LduPMX8b2AsOzI0rQh+4bjohOZvKmGKiuPv3eSvqpi/r6208ZVTBjjFvBO"
            , "UQhMbPUyR6vO1ryFDwBMwMqQ06ldkXArhB+SG0dYnOKb/6g0nO2BVFUCgYEAwBeH"
            , "HGNGuxwum63UAaqyX6lRSpGGm6XSCBhzvHUPnVphgq7nnZOGl0z3U49jreCvuuEc"
            , "fA9YqxJjzoZy5870KOXY2kltlq/U/4Lrb0k75ag6ZVbi0oemACN6KCHtE+Zm2dac"
            , "rW8oKWpRTbsvMOYUvSjF0u8BCrestpRUF977Ks8CgYEAicbLFCjK9+ozq+eJKPFO"
            , "eZ6BU6YWR2je5Z5D6i3CyzT+3whXvECzd6yLpXfrDyEbPTB5jUacbB0lTmWFb3fb"
            , "UK6n89bkCKO2Ab9/XKJxAkPzcgGmME+vLRx8w5v29STWAW78rj/H9ymPbqqTaJ82"
            , "GQ5+jBI1Sw6GeNAW+8P2pLECgYAs/dXBimcosCMih4ZelZKN4WSO6KL0ldQp3UBO"
            , "ZcSwgFjSeRD60XD2wyoywiUAtt2yEcPQMu/7saT63HbRYKHDaoJuLkCiyLBE4G8w"
            , "c6C527tBvSYHVYpGAgk8mSWkQZTZdPDhlmV7vdEpOayF8X3uCDy9eQlvbzHe2cMQ"
            , "jEOb9QKBgG3jSxGfqN/sD8W9BhpVrybCXh2RvhxOBJAFx58wSWTkRcYSwpdyvm7x"
            , "wlMtcEdQgaSBeuBU3HPUdYE07bQNAlYO0p9MQnsLHzd2V9yiCX1Sq5iB6dQpHxyi"
            , "sDZLY2Mym1nUJWfE47GAcxFZtrVh9ojKcmgiHo8qPTkWjFGY7xe/"
            , "-----END RSA PRIVATE KEY-----"
    ));

    public static final AwsSecret TAG_ROADRUNNER = new AwsSecret("roadrunner", "", Collections.singletonMap("product", "roadrunner"), "supersecret");

    public static final AwsSecret TAG_COYOTE = new AwsSecret("coyote", "", Collections.singletonMap("product", "coyote"), "supersecret");

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getValue() {
        return value;
    }
}
