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

package org.overlord.commons.idp;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.overlord.commons.config.OverlordConfig;
import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.identity.federation.core.impl.KeyStoreKeyManager;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;

/**
 * A version of the picketlink {@link TrustKeyManager} that can be configured
 * externally to the IDP WAR.  The configuration comes from the shared Overlord
 * configuration properties file.
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordIdpKeyStoreKeyManager extends KeyStoreKeyManager {
    
    private static final OverlordConfig overlord = new OverlordConfig();

    /**
     * Constructor.
     */
    public OverlordIdpKeyStoreKeyManager() {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.impl.KeyStoreKeyManager#setAuthProperties(java.util.List)
     */
    @Override
    public void setAuthProperties(List<AuthPropertyType> authList) throws TrustKeyConfigurationException,
            TrustKeyProcessingException {
        List<AuthPropertyType> auths = new ArrayList<AuthPropertyType>();
        auths.add(create(KEYSTORE_URL, overlord.getSamlKeystoreUrl()));
        auths.add(create(KEYSTORE_PASS, overlord.getSamlKeystorePassword()));
        auths.add(create(SIGNING_KEY_ALIAS, overlord.getSamlSigningKeyAlias()));
        auths.add(create(SIGNING_KEY_PASS, overlord.getSamlSigningKeyPassword()));
        super.setAuthProperties(auths);
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
