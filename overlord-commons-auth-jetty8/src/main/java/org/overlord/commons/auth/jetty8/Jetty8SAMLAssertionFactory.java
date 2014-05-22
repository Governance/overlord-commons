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

package org.overlord.commons.auth.jetty8;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.overlord.commons.auth.filters.HttpRequestThreadLocalFilter;
import org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter;
import org.overlord.commons.auth.filters.SimplePrincipal;
import org.overlord.commons.auth.util.SAMLAssertionFactory;
import org.overlord.commons.auth.util.SAMLBearerTokenUtil;

/**
 * An assertion factory for use in Jetty 8.
 *
 * @author eric.wittmann@redhat.com
 */
public class Jetty8SAMLAssertionFactory implements SAMLAssertionFactory {
    
    /**
     * Constructor.
     */
    public Jetty8SAMLAssertionFactory() {
    }

    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#accept()
     */
    @Override
    public boolean accept() {
        try {
            Class.forName("org.eclipse.jetty.server.Request"); //$NON-NLS-1$
            return true;
        } catch (ClassNotFoundException e) {
        }
        return false;
    }
    
    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#createSAMLAssertion(java.lang.String, java.lang.String, int)
     */
    @Override
    public String createSAMLAssertion(String issuerName, String forService, int timeValidInMillis) {
        try {
            // Try our thread local first.  If we're using our own authentication mechanism,
            // we would have stored it in the ThreadLocal for just this purpose.
            SimplePrincipal sp = SamlBearerTokenAuthFilter.TL_principal.get();
            if (sp != null) {
                return SAMLBearerTokenUtil.createSAMLAssertion(sp, sp.getRoles(), issuerName, forService);
            }

            // If that didn't work, try to rip apart the current request, with specific
            // Jetty knowledge.
            HttpServletRequest request = HttpRequestThreadLocalFilter.TL_request.get();
            Request jettyRequest = (Request) request;
            Authentication authentication = jettyRequest.getAuthentication();
            User userAuth = (User) authentication;
            UserIdentity userIdentity = userAuth.getUserIdentity();
            Principal userPrincipal = userIdentity.getUserPrincipal();
            Subject subject = userIdentity.getSubject();
            Set<String> roles = new HashSet<String>();
            for (String cname : JettyAuthConstants.ROLE_CLASSES) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends Principal> c = (Class<? extends Principal>) Thread.currentThread().getContextClassLoader().loadClass(cname);
                    Set<? extends Principal> principals = subject.getPrincipals(c);
                    for (Principal p : principals) {
                        roles.add(p.getName());
                    }
                } catch (ClassNotFoundException e) {
                    // Skip it!
                }
            }
            return SAMLBearerTokenUtil.createSAMLAssertion(userPrincipal, roles, issuerName, forService, timeValidInMillis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
