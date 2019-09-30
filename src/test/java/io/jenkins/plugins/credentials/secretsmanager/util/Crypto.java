package io.jenkins.plugins.credentials.secretsmanager.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Encapsulate BouncyCastle operations.
 */
public abstract class Crypto {

    private Crypto() {

    }

    public static String newPrivateKey() {
        final PrivateKey privateKey = newKeyPair().getPrivate();

        try (StringWriter sw = new StringWriter()) {
            final JcaPEMWriter writer = new JcaPEMWriter(sw);

            writer.writeObject(privateKey);
            writer.close();

            return sw.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair newKeyPair() {
        final KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(512);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] saveKeyStore(KeyStore keyStore, char[] password) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            keyStore.store(baos, password);
            return baos.toByteArray();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyStore newKeyStore(char[] password) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static X509Certificate newSelfSignedCertificate(KeyPair keyPair) {
        try {
            final X500Name cn = new X500Name("CN=localhost");
            final X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                    cn,
                    BigInteger.valueOf(Math.abs(new SecureRandom().nextInt())),
                    new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30)), // Not after 99 days from now
                    new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 99)), // Subject
                    cn,
                    keyPair.getPublic()
            );
            final X509CertificateHolder holder = b.build(new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate()));
            return new JcaX509CertificateConverter().getCertificate(holder);
        } catch (CertificateException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }
}