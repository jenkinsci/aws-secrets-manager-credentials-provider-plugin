package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.Secret;

abstract class Fixtures {
    static final char[] EMPTY_PASSWORD = {};
    static final Secret EMPTY_PASSPHRASE = Secret.fromString("");
    static final String SSH_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICXQIBAAKBgQDbyoNEw32kJTN3/Bgnhr2GDlJ74YbwaFXMLC2V2j98+384NYra\n" +
            "/mDOsBBU9eBLH7dKMGLvaTFGzUliIAASrJSoWAxJGaAKwHPnkG44gpf+wKQNybXn\n" +
            "54vsNqDRFuz0BzDcD3YKEtqdT2eK/wJn80uarnQl4QcPfZx8/ELuVxo3uwIDAQAB\n" +
            "AoGBAMCc26byTvP/qfg3U4+oFAUcHgr0XIXoWXAhMx3E8qh72kSPH43FKV9Yiid6\n" +
            "hkIvnDgG6Vz36bgrhWjZtFapKWgyFZ2sRbrcU5+4Ks3+96V7abSF71KkWl/WWvMQ\n" +
            "61/Q5lC6ZRFiLVdI4JJhAgdu0qrDCTxHMYtkxzYAvNGE4jX5AkEA9rsBVdyNGi85\n" +
            "VRXHxs/0EG/56okBaMuVpd9l4noPcWRlrWwCYU/paExZimYyy5+5hp4TcTmQLw2Q\n" +
            "jRo3cj5SHwJBAOQMaR6jfsgKIydqC9ZqcIH3YKI7AV/e3+3qXL3GHPcVqJVIMGSF\n" +
            "DHqFW42B2WjdSpG4k4LisIBblsuHpXQg/uUCQAE2VgFX/hF83ek/HCYr62URR8cR\n" +
            "OUKMjYWtHVEJjH3gImfBuhlETT9H8MCvU9yQQlcY+7t4ru6sQGORF2imSb0CQQCb\n" +
            "5agPG/HVyqhRj3tcLxOOpZBYF0JPScuHl4mi6kZu202OD/WVIidvsq7tw/DecTlC\n" +
            "+Q1Okq3accJajPacttnJAkAhM+1WigW6myaRQYkr648Vo47RFbtAJzn+RTY46sEn\n" +
            "srzbfLspVubVfrJ/kh4LIEwPapfxPb7QQeK0guUABL/B\n" +
            "-----END RSA PRIVATE KEY-----";

    private Fixtures() {

    }
}
