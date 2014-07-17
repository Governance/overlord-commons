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

package org.overlord.commons.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.Configuration;

/**
 * Core/shared overlord configuration.
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordConfig {

    public static final String OVERLORD_CONFIG_FILE_NAME     = "overlord.config.file.name"; //$NON-NLS-1$
    public static final String OVERLORD_CONFIG_FILE_REFRESH  = "overlord.config.file.refresh"; //$NON-NLS-1$

    public static Configuration overlordConfig;
    static {
        String configFile = System.getProperty(OVERLORD_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(OVERLORD_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        overlordConfig = ConfigurationFactory.createConfig(
                configFile,
                "overlord.properties", //$NON-NLS-1$
                refreshDelay,
                null, null);
    }

    protected static final String SAML_KEYSTORE = "overlord.auth.saml-keystore"; //$NON-NLS-1$
    protected static final String SAML_KEYSTORE_PASSWORD = "overlord.auth.saml-keystore-password"; //$NON-NLS-1$
    protected static final String SAML_KEY_ALIAS = "overlord.auth.saml-key-alias"; //$NON-NLS-1$
    protected static final String SAML_KEY_ALIAS_PASSWORD = "overlord.auth.saml-key-alias-password"; //$NON-NLS-1$

    private String keystore;
    
    /**
     * @return the SAML keystore url
     */
    public String getSamlKeystoreUrl() {
        if (keystore == null) {
            String ks = overlordConfig.getString(SAML_KEYSTORE);
            File ksFile = new File(ks);
            try {
                if (ksFile.isFile()) {
                    ks = ksFile.toURI().toURL().toString();
                } else {
                    throw new FileNotFoundException(ks);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            keystore = ks;
        }
        return keystore;
    }
    
    /**
     * @return the SAML keystore password
     */
    public String getSamlKeystorePassword() {
        return overlordConfig.getString(SAML_KEYSTORE_PASSWORD);
    }
    
    /**
     * @return the SAML signing key alias
     */
    public String getSamlSigningKeyAlias() {
        return overlordConfig.getString(SAML_KEY_ALIAS);
    }
    
    /**
     * @return the SAML signing key password
     */
    public String getSamlSigningKeyPassword() {
        return overlordConfig.getString(SAML_KEY_ALIAS_PASSWORD);
    }

}
