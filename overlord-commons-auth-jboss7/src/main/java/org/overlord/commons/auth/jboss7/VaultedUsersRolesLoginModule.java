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
package org.overlord.commons.auth.jboss7;

import org.jboss.security.auth.spi.UsersRolesLoginModule;
import org.jboss.security.vault.SecurityVaultException;
import org.jboss.security.vault.SecurityVaultUtil;

/**
 * Extends the {@link UsersRolesLoginModule} to add support for vaulted passwords.
 *
 * @author eric.wittmann@redhat.com
 */
public class VaultedUsersRolesLoginModule extends UsersRolesLoginModule {

    /**
     * Constructor.
     */
    public VaultedUsersRolesLoginModule() {
    }

    /**
     * @see org.jboss.security.auth.spi.UsersRolesLoginModule#getUsersPassword()
     */
    @Override
    protected String getUsersPassword() {
        String pwd = super.getUsersPassword();
        if (SecurityVaultUtil.isVaultFormat(pwd)) {
            try {
                pwd = SecurityVaultUtil.getValueAsString(pwd);
            } catch (SecurityVaultException e) {
                throw new RuntimeException(e);
            }
        }
        return pwd;
    }

}
