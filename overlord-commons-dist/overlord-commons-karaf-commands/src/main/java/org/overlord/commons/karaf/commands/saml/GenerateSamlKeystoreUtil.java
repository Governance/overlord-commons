/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.commons.karaf.commands.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.overlord.commons.karaf.commands.i18n.Messages;

/**
 * Saml Keystore generator util. It generates a keystore file based on a
 * password. It is based on the KeyGen source code, but using the bouncy castle
 * keypair generation.
 *
 *
 * @author David Virgil Naranjo
 */
public class GenerateSamlKeystoreUtil {

    private final static int validity = 90;
    private char[] keyPass = null;
    private char[] storePass = null;
    private final String providerName = null;
    private KeyStore keyStore = null;
    private String storetype = null;
    private String srcstoretype = null;

    private String alias = null;
    private String dname = null;
    private String keyAlgName = null;
    private int keysize = -1;
    private final String startDate = null;

    /**
     * Instantiates a new generate saml keystore util.
     */
    public GenerateSamlKeystoreUtil() {
        storetype = "jks"; //$NON-NLS-1$
        alias = "overlord"; //$NON-NLS-1$
        dname = "CN=Picketbox vault, OU=picketbox, O=Jboss, L=Westford, ST=Mass, C=US"; //$NON-NLS-1$
        keysize = 2048;
        keyAlgName = "RSA"; //$NON-NLS-1$
    }

    /**
     * Generate.
     *
     * @param password
     *            the password
     * @param outputFile
     *            the output file
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    public boolean generate(String password, File outputFile) throws Exception {
        storePass = password.toCharArray();
        keyPass = password.toCharArray();
        if (storetype == null) {
            storetype = KeyStore.getDefaultType();
        }

        if (srcstoretype == null) {
            srcstoretype = KeyStore.getDefaultType();
        }

        if (providerName == null) {
            keyStore = KeyStore.getInstance(storetype);
        } else {
            keyStore = KeyStore.getInstance(storetype, providerName);
        }
        keyStore.load(null, storePass);

        doGenKeyPair(alias, dname, keyAlgName, keysize, null);
        char[] pass = storePass;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        keyStore.store(bout, pass);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileOutputStream fout = new FileOutputStream(outputFile);
        try {
            fout.write(bout.toByteArray());
        } finally {
            fout.close();
        }

        return true;
    }

    /**
     * Creates a new key pair and self-signed certificate.
     *
     * @param alias
     *            the alias
     * @param dname
     *            the dname
     * @param keyAlgName
     *            the key alg name
     * @param keysize
     *            the keysize
     * @param sigAlgName
     *            the sig alg name
     * @throws Exception
     *             the exception
     */
    private void doGenKeyPair(String alias, String dname, String keyAlgName, int keysize, String sigAlgName) throws Exception {

        if (keysize == -1) {
            if ("EC".equalsIgnoreCase(keyAlgName)) { //$NON-NLS-1$
                keysize = 256;
            } else if ("RSA".equalsIgnoreCase(keyAlgName)) { //$NON-NLS-1$
                keysize = 2048;
            } else {
                keysize = 1024;
            }
        }

        if (keyStore.containsAlias(alias)) {
            throw new Exception(Messages.getString("Key.pair.not.generated.alias.alias.already.exists")); //$NON-NLS-1$
        }

        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(keyAlgName);
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance(keyAlgName);
        generator.initialize(keysize);
        KeyPair keypair=generator.generateKeyPair();
        PrivateKey privKey = keypair.getPrivate();


        X509Certificate[] chain  = new X509Certificate[1];


        Date date = getStartDate(startDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // Configuramos la fecha que se recibe
       calendar.add(Calendar.DAY_OF_YEAR, validity);
        // time from which certificate is valid
        Date expiryDate = calendar.getTime();            // time after which certificate is not valid


        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(dname), new BigInteger("1"), date, expiryDate, new X500Name(
                dname), SubjectPublicKeyInfo.getInstance(keypair.getPublic().getEncoded()));
        byte[] certBytes = certBuilder.build(new JCESigner(privKey, "SHA256withRSA")).getEncoded();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));

        chain[0] = certificate;

        keyStore.setKeyEntry(alias, privKey, keyPass, chain);
    }

    /**
     * Gets the compatible sig alg name.
     *
     * @param keyAlgName
     *            the key alg name
     * @return the compatible sig alg name
     * @throws Exception
     *             the exception
     */
    private static String getCompatibleSigAlgName(String keyAlgName) throws Exception {
        if ("DSA".equalsIgnoreCase(keyAlgName)) { //$NON-NLS-1$
            return "SHA1WithDSA"; //$NON-NLS-1$
        } else if ("RSA".equalsIgnoreCase(keyAlgName)) { //$NON-NLS-1$
            return "SHA256WithRSA"; //$NON-NLS-1$
        } else if ("EC".equalsIgnoreCase(keyAlgName)) { //$NON-NLS-1$
            return "SHA256withECDSA"; //$NON-NLS-1$
        } else {
            throw new Exception(Messages.getString("Cannot.derive.signature.algorithm")); //$NON-NLS-1$
        }
    }

    /**
     * Returns the issue time that's specified the -startdate option.
     *
     * @param s
     *            the value of -startdate option
     * @return the start date
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static Date getStartDate(String s) throws IOException {
        Calendar c = new GregorianCalendar();
        return c.getTime();
    }

    private static class JCESigner implements ContentSigner {

        private static final AlgorithmIdentifier PKCS1_SHA256_WITH_RSA_OID = new AlgorithmIdentifier(
                new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"));

        private Signature signature;
        private ByteArrayOutputStream outputStream;

        public JCESigner(PrivateKey privateKey, String signatureAlgorithm) {
            if (!"SHA256withRSA".equals(signatureAlgorithm)) {
                throw new IllegalArgumentException("Signature algorithm \"" + signatureAlgorithm + "\" not yet supported");
            }
            try {
                this.outputStream = new ByteArrayOutputStream();
                this.signature = Signature.getInstance(signatureAlgorithm);
                this.signature.initSign(privateKey);
            } catch (GeneralSecurityException gse) {
                throw new IllegalArgumentException(gse.getMessage());
            }
        }

        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier() {
            if (signature.getAlgorithm().equals("SHA256withRSA")) {
                return PKCS1_SHA256_WITH_RSA_OID;
            } else {
                return null;
            }
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public byte[] getSignature() {
            try {
                signature.update(outputStream.toByteArray());
                return signature.sign();
            } catch (GeneralSecurityException gse) {
                gse.printStackTrace();
                return null;
            }
        }
    }
}
