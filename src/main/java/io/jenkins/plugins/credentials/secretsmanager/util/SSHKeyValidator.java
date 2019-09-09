package io.jenkins.plugins.credentials.secretsmanager.util;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public abstract class SSHKeyValidator {

    private static Validator chain = new ValidatorChain(Arrays.asList(new PEMKeyPairValidator(), new PrivateKeyInfoValidator(), new OpenSSHPrivateKeyValidator()));

    private SSHKeyValidator() {

    }

    public static boolean isValid(String str) {
        if (str == null) {
            return false;
        }

        return chain.isValid(str);
    }

    private interface Validator {
        boolean isValid(String str);
    }

    private static class ValidatorChain implements Validator {

        private final List<Validator> chain;

        private ValidatorChain(List<Validator> chain) {
            this.chain = chain;
        }

        @Override
        public boolean isValid(String str) {
            return this.chain.stream().anyMatch(m -> m.isValid(str));
        }
    }

    private static class PEMKeyPairValidator implements Validator {

        @Override
        public boolean isValid(String str) {
            final PEMParser parser = new PEMParser(new StringReader(str));
            try {
                final Object object = parser.readObject();
                return (object instanceof PEMKeyPair);
            } catch (IOException e) {
                return false;
            }
        }
    }

    private static class PrivateKeyInfoValidator implements Validator {

        @Override
        public boolean isValid(String str) {
            final PEMParser parser = new PEMParser(new StringReader(str));
            try {
                final Object object = parser.readObject();
                return (object instanceof PrivateKeyInfo);
            } catch (IOException e) {
                return false;
            }
        }
    }

    private static class OpenSSHPrivateKeyValidator implements Validator {

        @Override
        public boolean isValid(String str) {
            // The OpenSSH private key format is not like other standard key formats.
            // Bouncycastle does not yet fully support parsing OpenSSH private keys, so we can only test
            // whether the key looks 'roughly' correct - does it have the right header, and does it have
            // some content.
            final PemReader reader = new PemReader(new StringReader(str));

            try {
                final PemObject obj = reader.readPemObject();
                return (obj != null) && obj.getType().equals("OPENSSH PRIVATE KEY") && (obj.getContent().length > 0);
            } catch (IOException e) {
                return false;
            }
        }
    }

}
