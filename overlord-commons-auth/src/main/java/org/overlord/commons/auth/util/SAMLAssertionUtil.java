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

package org.overlord.commons.auth.util;

import org.overlord.commons.auth.jboss7.Jboss7SAMLBearerTokenUtil;
import org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule;
import org.overlord.commons.auth.tomcat.HttpRequestThreadLocalValve;
import org.overlord.commons.auth.tomcat.TomcatSAMLBearerTokenUtil;

/**
 * A single entry point for creating SAML assertions.  This class delegates to the appropriate
 * util depending on the current system.
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLAssertionUtil {

    /**
     * Creates a SAML Assertion that can be used as a bearer token when invoking REST
     * services.  The REST service must be configured to accept SAML Assertion bearer
     * tokens.
     * 
     * In JBoss this means protecting the REST services with {@link SAMLBearerTokenLoginModule}.
     * In Tomcat7 this means protecting the REST services with {@link SAMLBearerTokenAuthenticator}.
     * 
     * @param issuerName the issuer name (typically the context of the calling web app)
     * @param forService the web context of the REST service being invoked
     */
    public static String createSAMLAssertion(String issuerName, String forService) {
        if (isJBoss7()) {
            return Jboss7SAMLBearerTokenUtil.createSAMLAssertion(issuerName, forService);
        }
        if (isTomcat()) {
            return TomcatSAMLBearerTokenUtil.createSAMLAssertion(HttpRequestThreadLocalValve.TL_request.get(), issuerName, forService);
        }
        throw new RuntimeException("Unsupported/undetected platform.");
    }

    /**
     * Determines if we're running in JBoss.
     */
    private static boolean isJBoss7() {
        String property = System.getProperty("jboss.server.config.dir");
        if (property != null)
            return true;
        return false;
    }

    /**
     * Determines if we're running in tomcat.
     */
    private static boolean isTomcat() {
        if (HttpRequestThreadLocalValve.TL_request.get() != null) {
            return true;
        }
        return false;
    }

}
