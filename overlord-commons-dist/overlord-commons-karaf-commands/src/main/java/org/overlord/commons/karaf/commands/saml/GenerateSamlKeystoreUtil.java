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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saml Keystore generator util. It generates a keystore file based on a
 * password. It is based on the KeyGen source code, but using the bouncy castle
 * keypair generation.
 *
 *
 * @author David Virgil Naranjo
 */
public class GenerateSamlKeystoreUtil {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSamlKeystoreUtil.class);

    private final static int validity = 90;
    private char[] keyPass = null;
    private char[] storePass = null;
    private final String providerName = null;
    private KeyStore keyStore = null;
    private String storetype = null;
    private String srcstoretype = null;
    private static final String P12KEYSTORE = "PKCS12";

    private String alias = null;
    private String dname = null;
    private String keyAlgName = null;
    private int keysize = -1;
    private final String startDate = null;

    /**
     * Instantiates a new generate saml keystore util.
     */
    public GenerateSamlKeystoreUtil() {
        storetype = "jks";
        alias = "overlord";
        dname = "CN=Picketbox vault, OU=picketbox, O=Jboss, L=Westford, ST=Mass, C=US";
        keysize = 2048;
        keyAlgName = "RSA";
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
            if ("EC".equalsIgnoreCase(keyAlgName)) {
                keysize = 256;
            } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
                keysize = 2048;
            } else {
                keysize = 1024;
            }
        }

        if (keyStore.containsAlias(alias)) {
            throw new Exception(Messages.getString("Key.pair.not.generated.alias.alias.already.exists"));
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
        BigInteger serialNumber =new BigInteger("10");       // serial number for certificate
                 // private key of the certifying authority (ca) certificate

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal              subjectName = new X500Principal(dname);

        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(subjectName);
        certGen.setNotBefore(date);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(subjectName);
        certGen.setPublicKey(keypair.getPublic());
        certGen.setSignatureAlgorithm("SHA256withRSA");

        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
 new SubjectKeyIdentifierStructure(keypair.getPublic()));

        X509Certificate cert = certGen.generate(keypair.getPrivate(), providerName);
        chain[0] = cert;

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
        if ("DSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA1WithDSA";
        } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA256WithRSA";
        } else if ("EC".equalsIgnoreCase(keyAlgName)) {
            return "SHA256withECDSA";
        } else {
            throw new Exception(Messages.getString("Cannot.derive.signature.algorithm"));
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

}
