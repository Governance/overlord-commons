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

package org.overlord.commons.config.saml.sp;

import org.overlord.commons.auth.saml.sp.KeyManagerConfigProvider;
import org.overlord.commons.config.OverlordConfig;

/**
 * A config provider that pulls the relevant information from the overlord.properties file.
 *
 * @author eric.wittmann@redhat.com
 */
public class SPKeyManagerConfigProvider implements KeyManagerConfigProvider {
    
    private static final OverlordConfig config = new OverlordConfig();
    
    /**
     * Constructor.
     */
    public SPKeyManagerConfigProvider() {
    }

    /**
     * @see org.overlord.commons.auth.saml.sp.KeyManagerConfigProvider#getSamlKeystoreUrl()
     */
    @Override
    public String getSamlKeystoreUrl() {
        return config.getSamlKeystoreUrl();
    }

    /**
     * @see org.overlord.commons.auth.saml.sp.KeyManagerConfigProvider#getSamlKeystorePassword()
     */
    @Override
    public String getSamlKeystorePassword() {
        return config.getSamlKeystorePassword();
    }

    /**
     * @see org.overlord.commons.auth.saml.sp.KeyManagerConfigProvider#getSamlSigningKeyAlias()
     */
    @Override
    public String getSamlSigningKeyAlias() {
        return config.getSamlSigningKeyAlias();
    }

    /**
     * @see org.overlord.commons.auth.saml.sp.KeyManagerConfigProvider#getSamlSigningKeyPassword()
     */
    @Override
    public String getSamlSigningKeyPassword() {
        return config.getSamlSigningKeyPassword();
    }

}
