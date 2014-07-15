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

package org.overlord.commons.auth.saml.sp;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.identity.federation.core.impl.KeyStoreKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;

/**
 * A version of the picketlink {@link TrustKeyManager} that can be configured
 * externally to the SP WAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordSPKeyStoreKeyManager extends KeyStoreKeyManager {
    
    private KeyManagerConfigProvider configProvider;
    
    /**
     * Constructor.
     */
    public OverlordSPKeyStoreKeyManager() {
    }

    /**
     * @see org.picketlink.identity.federation.core.impl.KeyStoreKeyManager#setAuthProperties(java.util.List)
     */
    @Override
    public void setAuthProperties(List<AuthPropertyType> authList) throws TrustKeyConfigurationException,
            TrustKeyProcessingException {
        configProvider = createConfigProvider(authList);
        
        List<AuthPropertyType> auths = new ArrayList<AuthPropertyType>();
        auths.add(create(KEYSTORE_URL, configProvider.getSamlKeystoreUrl()));
        auths.add(create(KEYSTORE_PASS, configProvider.getSamlKeystorePassword()));
        auths.add(create(SIGNING_KEY_ALIAS, configProvider.getSamlSigningKeyAlias()));
        auths.add(create(SIGNING_KEY_PASS, configProvider.getSamlSigningKeyPassword()));
        super.setAuthProperties(auths);
    }
    
    /**
     * Creates the config provider from information found in the picketlink.xml file.
     * @param authList
     */
    private KeyManagerConfigProvider createConfigProvider(List<AuthPropertyType> authList) {
        try {
            for (AuthPropertyType auth : authList) {
                if (auth.getKey().equals("ConfigProviderClass")) { //$NON-NLS-1$
                    String classname = auth.getValue();
                    Class<?> clazz = loadClass(classname);
                    return (KeyManagerConfigProvider) clazz.newInstance();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Configuration error: could not create 'ConfigProviderClass' specified in picketlink.xml", e); //$NON-NLS-1$
        }
        throw new RuntimeException("Configuration error: no 'ConfigProviderClass' specified in picketlink.xml"); //$NON-NLS-1$
    }

    /**
     * Loads the given class.
     * @param classname
     */
    private Class<?> loadClass(String classname) {
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(classname);
        } catch (ClassNotFoundException e) {
        }
        try {
            return getClass().getClassLoader().loadClass(classname);
        } catch (ClassNotFoundException e) {
        }
        throw new RuntimeException("Failed to load config provider class: " + classname); //$NON-NLS-1$
    }

    /**
     * Creates an {@link AuthPropertyType} object given a key/value pair.
     * @param key
     * @param value
     */
    private AuthPropertyType create(String key, String value) {
        AuthPropertyType rval = new AuthPropertyType();
        rval.setKey(key);
        rval.setValue(value);
        return rval;
    }

    /**
     * @see org.picketlink.identity.federation.core.impl.KeyStoreKeyManager#getValidatingKey(java.lang.String)
     */
    @Override
    public PublicKey getValidatingKey(String domain) throws TrustKeyConfigurationException,
            TrustKeyProcessingException {
        return getSigningKeyPair().getPublic();
    }

}
