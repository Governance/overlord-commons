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

package org.overlord.commons.auth.util;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.overlord.commons.auth.filters.HttpRequestThreadLocalFilter;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * A SAML assertion factory that works with a simple filter-based 
 * PicketLink SP app.
 */
public class StandardSAMLAssertionFactory implements SAMLAssertionFactory {
    
    /**
     * C'tor.
     */
    public StandardSAMLAssertionFactory() {
    }

    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#accept()
     */
    @Override
    public boolean accept() {
        HttpServletRequest request = HttpRequestThreadLocalFilter.TL_request.get();
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession();
        return session.getAttribute(GeneralConstants.PRINCIPAL_ID) != null && session.getAttribute(GeneralConstants.ROLES_ID) != null;
    }

    /**
     * @see org.overlord.commons.auth.util.SAMLAssertionFactory#createSAMLAssertion(java.lang.String, java.lang.String, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String createSAMLAssertion(String issuerName, String forService, int timeValidInMillis) {
        HttpServletRequest request = HttpRequestThreadLocalFilter.TL_request.get();
        if (request == null) {
            throw new RuntimeException("Failed to create SAML assertion: could not locate HTTP Request.");
        }
        HttpSession session = request.getSession();
        Principal principal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
        List<String> roles = (List<String>) session.getAttribute(GeneralConstants.ROLES_ID);
        return SAMLBearerTokenUtil.createSAMLAssertion(principal, new HashSet<String>(roles), issuerName, forService, timeValidInMillis);
    }

}
