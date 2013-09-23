/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.commons.config.vault;

import org.apache.commons.lang.text.StrLookup;
import org.jboss.security.vault.SecurityVaultUtil;

/**
 * Implements config property interpolation of type "vault".  This allows JBoss EAP
 * password vault formatted properties to be expanded.  This will only work when
 * running in JBoss EAP or JBoss AS 7.1.  It fails silently in other environments.
 *
 * @author eric.wittmann@redhat.com
 */
public class VaultLookup extends StrLookup {

    /**
     * Constructor.
     */
    public VaultLookup() {
    }

    /**
     * @see org.apache.commons.lang.text.StrLookup#lookup(java.lang.String)
     */
    @Override
    public String lookup(String key) {
        try {
            return SecurityVaultUtil.getValueAsString(key);
        } catch (Throwable t) {
            // Eat it - if something goes wrong, too bad - we're probably not running in jboss
        }
        return null;
    }

}
