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

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.jboss.security.SecurityContextAssociation;
import org.overlord.commons.auth.util.SAMLAssertionFactory;
import org.overlord.commons.auth.util.SAMLBearerTokenUtil;

/**
 * JBoss 7 implementation of a SAML Assertion factory.
 * 
 * @author eric.wittmann@redhat.com
 */
public class Jboss7SAMLAssertionFactory implements SAMLAssertionFactory {

    /**
     * Constructor.
     */
    public Jboss7SAMLAssertionFactory() {
    }

    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#accept()
     */
    @Override
    public boolean accept() {
        String property = System.getProperty("jboss.server.config.dir");
        if (property != null) {
            File f = new File(property, "standalone.xml");
            if (f.isFile())
                return true;
        }
        return false;
    }

    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#createSAMLAssertion(java.lang.String, java.lang.String, int)
     */
    @Override
    public String createSAMLAssertion(String issuerName, String forService, int timeValidInMillis) {
        try {
            Principal principal = SecurityContextAssociation.getPrincipal();
            Set<Principal> userRoles = SecurityContextAssociation.getSecurityContext()
                    .getAuthorizationManager().getUserRoles(principal);
            Set<String> roles = new HashSet<String>();
            if (userRoles != null) {
                for (Principal role : userRoles) {
                    roles.add(role.getName());
                }
            }
            return SAMLBearerTokenUtil.createSAMLAssertion(principal, roles, issuerName, forService, timeValidInMillis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
