package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SshKeyValidatorTest {

    private static final Case BAD_CONTENT_PRIVATE_KEY = new Case.Builder()
            .withDescription("Private key with bad content")
            .withKey("-----BEGIN OPENSSH PRIVATE KEY-----",
                     "abcdef")
            .isNotValid();

    private static final Case BAD_HEADER_PRIVATE_KEY = new Case.Builder()
            .withDescription("Private key with bad header")
            .withKey("-----INVALID PRIVATE KEY")
            .isNotValid();

    private static final Case EMPTY_OPENSSH_PRIVATE_KEY = new Case.Builder()
            .withDescription("Empty OpenSSH private key")
            .withKey("-----BEGIN OPENSSH PRIVATE KEY-----",
                     "-----END OPENSSH PRIVATE KEY-----")
            .isNotValid();

    private static final Case EMPTY_PKCS1_PRIVATE_KEY = new Case.Builder()
            .withDescription("Empty PKCS#1 private key")
            .withKey("-----BEGIN RSA PRIVATE KEY-----",
                     "-----END RSA PRIVATE KEY-----")
            .isNotValid();

    private static final Case EMPTY_PKCS8_PRIVATE_KEY = new Case.Builder()
            .withDescription("Empty PKCS#8 private key")
            .withKey("-----BEGIN PRIVATE KEY-----",
                     "-----END PRIVATE KEY-----")
            .isNotValid();

    private static final Case OPENSSH_PRIVATE_KEY = new Case.Builder()
            .withDescription("OpenSSH private key")
            .withKey("-----BEGIN OPENSSH PRIVATE KEY-----",
                     "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAlwAAAAdzc2gtcn",
                     "NhAAAAAwEAAQAAAIEAvo3kQ4F54OnF/B/pVHLy1YECC15Pdh0HOQJDmrSm4WhlMVVHjnSc",
                     "WrYmvCar4njMsU50+W9lAbPkKE79jWkgiSRjegQ1h7lknRxDMAxqt9hPX4ubIEIKilgCnz",
                     "BGKOdaxae0YAyaml7v4CYwBWRMiZbSHhwuBv3Ms4HRGnfGxvMAAAII8gSeVvIEnlYAAAAH",
                     "c3NoLXJzYQAAAIEAvo3kQ4F54OnF/B/pVHLy1YECC15Pdh0HOQJDmrSm4WhlMVVHjnScWr",
                     "YmvCar4njMsU50+W9lAbPkKE79jWkgiSRjegQ1h7lknRxDMAxqt9hPX4ubIEIKilgCnzBG",
                     "KOdaxae0YAyaml7v4CYwBWRMiZbSHhwuBv3Ms4HRGnfGxvMAAAADAQABAAAAgFrqzl9bFn",
                     "C2eW1LOIO/eJdfvz73V4huXzTXHLRiv0DLE4UPQF36y2MIh8C73sTmiBuM6Ijeml3Om+yT",
                     "i6x93TRdiUdBkUyfOXb2BohZPa9kpL0GRq00vHlj0n4uFXjXFIXsBRbnKFYPnBfDkXO4SA",
                     "wKzQ8SivZtPCPxPAv8GyTpAAAAQHb9iJDAg3XhJDafM7Gd9cyM5hC9ERN+sM5oAyK/YJ27",
                     "fjPV3KS2mJ2V+T/8RV5izorjnj4o7TA0yV1lyUZDUcIAAABBAPpgKemv0d9idEKRUdUTzr",
                     "ZDAcHzvVgiPIIY28iqz8TIYNPKJ4V55nj90VXbUL2+oy6hmYgfx73Ma9hkbWAYP28AAABB",
                     "AMLVtn/82sM5Jnjc40T4efNXG8BGIxFjS/Ek5ey223F//UpOrzR0B0iSxjSztrUtoE2B1z",
                     "Jkcs9sxdyX70AmLr0AAAAQYWNtZUBleGFtcGxlLmNvbQECAw==",
                     "-----END OPENSSH PRIVATE KEY-----")
            .isValid();

    private static final Case PKCS1_PRIVATE_KEY = new Case.Builder()
        .withDescription("PKCS#1 private key")
        .withKey("-----BEGIN RSA PRIVATE KEY-----",
                 "MIICXQIBAAKBgQDbyoNEw32kJTN3/Bgnhr2GDlJ74YbwaFXMLC2V2j98+384NYra",
                 "/mDOsBBU9eBLH7dKMGLvaTFGzUliIAASrJSoWAxJGaAKwHPnkG44gpf+wKQNybXn",
                 "54vsNqDRFuz0BzDcD3YKEtqdT2eK/wJn80uarnQl4QcPfZx8/ELuVxo3uwIDAQAB",
                 "AoGBAMCc26byTvP/qfg3U4+oFAUcHgr0XIXoWXAhMx3E8qh72kSPH43FKV9Yiid6",
                 "hkIvnDgG6Vz36bgrhWjZtFapKWgyFZ2sRbrcU5+4Ks3+96V7abSF71KkWl/WWvMQ",
                 "61/Q5lC6ZRFiLVdI4JJhAgdu0qrDCTxHMYtkxzYAvNGE4jX5AkEA9rsBVdyNGi85",
                 "VRXHxs/0EG/56okBaMuVpd9l4noPcWRlrWwCYU/paExZimYyy5+5hp4TcTmQLw2Q",
                 "jRo3cj5SHwJBAOQMaR6jfsgKIydqC9ZqcIH3YKI7AV/e3+3qXL3GHPcVqJVIMGSF",
                 "DHqFW42B2WjdSpG4k4LisIBblsuHpXQg/uUCQAE2VgFX/hF83ek/HCYr62URR8cR",
                 "OUKMjYWtHVEJjH3gImfBuhlETT9H8MCvU9yQQlcY+7t4ru6sQGORF2imSb0CQQCb",
                 "5agPG/HVyqhRj3tcLxOOpZBYF0JPScuHl4mi6kZu202OD/WVIidvsq7tw/DecTlC",
                 "+Q1Okq3accJajPacttnJAkAhM+1WigW6myaRQYkr648Vo47RFbtAJzn+RTY46sEn",
                 "srzbfLspVubVfrJ/kh4LIEwPapfxPb7QQeK0guUABL/B",
                 "-----END RSA PRIVATE KEY-----")
        .isValid();

    private static final Case PKCS8_PRIVATE_KEY = new Case.Builder()
        .withDescription("PKCS#8 private key")
        .withKey("-----BEGIN PRIVATE KEY-----",
                 "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANvKg0TDfaQlM3f8",
                 "GCeGvYYOUnvhhvBoVcwsLZXaP3z7fzg1itr+YM6wEFT14Esft0owYu9pMUbNSWIg",
                 "ABKslKhYDEkZoArAc+eQbjiCl/7ApA3Jtefni+w2oNEW7PQHMNwPdgoS2p1PZ4r/",
                 "AmfzS5qudCXhBw99nHz8Qu5XGje7AgMBAAECgYEAwJzbpvJO8/+p+DdTj6gUBRwe",
                 "CvRchehZcCEzHcTyqHvaRI8fjcUpX1iKJ3qGQi+cOAbpXPfpuCuFaNm0VqkpaDIV",
                 "naxFutxTn7gqzf73pXtptIXvUqRaX9Za8xDrX9DmULplEWItV0jgkmECB27SqsMJ",
                 "PEcxi2THNgC80YTiNfkCQQD2uwFV3I0aLzlVFcfGz/QQb/nqiQFoy5Wl32Xieg9x",
                 "ZGWtbAJhT+loTFmKZjLLn7mGnhNxOZAvDZCNGjdyPlIfAkEA5AxpHqN+yAojJ2oL",
                 "1mpwgfdgojsBX97f7epcvcYc9xWolUgwZIUMeoVbjYHZaN1KkbiTguKwgFuWy4el",
                 "dCD+5QJAATZWAVf+EXzd6T8cJivrZRFHxxE5QoyNha0dUQmMfeAiZ8G6GURNP0fw",
                 "wK9T3JBCVxj7u3iu7qxAY5EXaKZJvQJBAJvlqA8b8dXKqFGPe1wvE46lkFgXQk9J",
                 "y4eXiaLqRm7bTY4P9ZUiJ2+yru3D8N5xOUL5DU6SrdpxwlqM9py22ckCQCEz7VaK",
                 "BbqbJpFBiSvrjxWjjtEVu0AnOf5FNjjqwSeyvNt8uylW5tV+sn+SHgsgTA9ql/E9",
                 "vtBB4rSC5QAEv8E=",
                 "-----END PRIVATE KEY-----")
        .isValid();

    private static final Case UNKNOWN_PKCS1_PRIVATE_KEY = new Case.Builder()
        .withDescription("Unknown PKCS#1 private key")
        .withKey("-----BEGIN UNKNOWN PRIVATE KEY-----",
                 "MIICXQIBAAKBgQDbyoNEw32kJTN3/Bgnhr2GDlJ74YbwaFXMLC2V2j98+384NYra",
                 "/mDOsBBU9eBLH7dKMGLvaTFGzUliIAASrJSoWAxJGaAKwHPnkG44gpf+wKQNybXn",
                 "54vsNqDRFuz0BzDcD3YKEtqdT2eK/wJn80uarnQl4QcPfZx8/ELuVxo3uwIDAQAB",
                 "AoGBAMCc26byTvP/qfg3U4+oFAUcHgr0XIXoWXAhMx3E8qh72kSPH43FKV9Yiid6",
                 "hkIvnDgG6Vz36bgrhWjZtFapKWgyFZ2sRbrcU5+4Ks3+96V7abSF71KkWl/WWvMQ",
                 "61/Q5lC6ZRFiLVdI4JJhAgdu0qrDCTxHMYtkxzYAvNGE4jX5AkEA9rsBVdyNGi85",
                 "VRXHxs/0EG/56okBaMuVpd9l4noPcWRlrWwCYU/paExZimYyy5+5hp4TcTmQLw2Q",
                 "jRo3cj5SHwJBAOQMaR6jfsgKIydqC9ZqcIH3YKI7AV/e3+3qXL3GHPcVqJVIMGSF",
                 "DHqFW42B2WjdSpG4k4LisIBblsuHpXQg/uUCQAE2VgFX/hF83ek/HCYr62URR8cR",
                 "OUKMjYWtHVEJjH3gImfBuhlETT9H8MCvU9yQQlcY+7t4ru6sQGORF2imSb0CQQCb",
                 "5agPG/HVyqhRj3tcLxOOpZBYF0JPScuHl4mi6kZu202OD/WVIidvsq7tw/DecTlC",
                 "+Q1Okq3accJajPacttnJAkAhM+1WigW6myaRQYkr648Vo47RFbtAJzn+RTY46sEn",
                 "srzbfLspVubVfrJ/kh4LIEwPapfxPb7QQeK0guUABL/B",
                 "-----END UNKNOWN PRIVATE KEY-----")
        .isNotValid();

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object> data() {
        return Arrays.asList(new Object[] {
                BAD_CONTENT_PRIVATE_KEY,
                BAD_HEADER_PRIVATE_KEY,
                EMPTY_OPENSSH_PRIVATE_KEY,
                EMPTY_PKCS1_PRIVATE_KEY,
                EMPTY_PKCS8_PRIVATE_KEY,
                OPENSSH_PRIVATE_KEY,
                PKCS1_PRIVATE_KEY,
                PKCS8_PRIVATE_KEY,
                UNKNOWN_PKCS1_PRIVATE_KEY,
        });
    }

    private final Case testCase;

    public SshKeyValidatorTest(Case testCase) {
        this.testCase = testCase;
    }

    @Test
    public void shouldValidate() {
        assertThat(SshKeyValidator.isValid(testCase.key)).isEqualTo(testCase.isValid);
    }

    private static class Case {
        private final String description;
        private final boolean isValid;
        private final String key;

        private Case(String description, boolean isValid, String key) {
            this.description = description;
            this.isValid = isValid;
            this.key = key;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder();
            s.append(description);
            s.append(" should ");
            if (!isValid) {
                s.append("not ");
            }
            s.append("be valid");
            return s.toString();
        }

        private static class Builder {
            private String description = "";
            private String key = null;

            Case.Builder withDescription(String d) {
                this.description = d;
                return this;
            }

            Case.Builder withKey(String... key) {
                this.key = String.join("\n", key);
                return this;
            }

            Case isValid() {
                return new Case(description, true, key);
            }

            Case isNotValid() {
                return new Case(description, false, key);
            }
        }
    }
}
