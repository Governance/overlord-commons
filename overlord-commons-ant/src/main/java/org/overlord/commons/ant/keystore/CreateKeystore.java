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
package org.overlord.commons.ant.keystore;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.picketbox.util.KeyStoreUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;

/**
 * @author Brett Meyer.
 */
public class CreateKeystore extends Task {

    private static final String KEYSTORE_TYPE = "JCEKS";

    private String keyStore;
    private String keyStorePassword;
    private int keySize;
    private String alias;

    @Override
    public void execute() throws BuildException {
        Console console = System.console();

        if (console == null) {
            throw new BuildException("\tConsole is not available");
        }

        assertProperty("keyStore", keyStore);
        assertProperty("keyStorePassword", keyStorePassword);
        assertProperty("keySize", keySize);
        assertProperty("alias", alias);

        try {
            File keyStoreFile = new File(keyStore);
            if (!keyStoreFile.exists()) {
                KeyStore ks = KeyStoreUtil.createKeyStore(KEYSTORE_TYPE, keyStorePassword.toCharArray());
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(keySize);
                SecretKey secretKey = keyGenerator.generateKey();
                KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
                KeyStore.PasswordProtection p = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
                ks.setEntry(alias, skEntry, p);
                ks.store(new FileOutputStream(keyStoreFile), keyStorePassword.toCharArray());
            }
            keyStore = keyStoreFile.getAbsolutePath();
        } catch (Exception e) {
            throw new BuildException("Problem creating keyStore: ", e);
        }
    }

    private void assertProperty(String propertyName, String propertyValue) throws BuildException {
        if (StringUtils.isBlank(propertyValue)) {
            throw new BuildException("\tThe " + propertyName + " property is required for this task.");
        }
    }

    private void assertProperty(String propertyName, int propertyValue) throws BuildException {
        if (propertyValue == 0) {
            throw new BuildException("\tThe " + propertyName + " property is required for this task.");
        }
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
