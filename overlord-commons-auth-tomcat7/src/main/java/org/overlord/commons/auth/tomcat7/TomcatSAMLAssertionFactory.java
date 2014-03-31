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
package org.overlord.commons.auth.tomcat7;

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.realm.GenericPrincipal;
import org.overlord.commons.auth.util.SAMLAssertionFactory;
import org.overlord.commons.auth.util.SAMLBearerTokenUtil;

/**
 * Class used to create SAML bearer tokens used when calling REST service
 * endpoints protected by SAML.
 *
 * @author eric.wittmann@redhat.com
 */
public class TomcatSAMLAssertionFactory implements SAMLAssertionFactory {
    
    /**
     * Constructor.
     */
    public TomcatSAMLAssertionFactory() {
    }
    
    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#accept()
     */
    @Override
    public boolean accept() {
        String property = System.getProperty("catalina.home"); //$NON-NLS-1$
        if (property != null) {
            File f = new File(property, "bin/catalina.sh"); //$NON-NLS-1$
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
            HttpServletRequest request = HttpRequestThreadLocalValve.TL_request.get();
            Principal principal = request.getUserPrincipal();
            if (principal instanceof GenericPrincipal) {
                GenericPrincipal gp = (GenericPrincipal) principal;
                String[] gpRoles = gp.getRoles();
                Set<String> roles = new HashSet<String>(gpRoles.length);
                for (String role : gpRoles) {
                    roles.add(role);
                }
                return SAMLBearerTokenUtil.createSAMLAssertion(principal, roles, issuerName, forService, timeValidInMillis);
            }
            throw new Exception("Unexpected/unsupported principal type: " + principal.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
