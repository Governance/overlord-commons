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

import java.util.Set;

import org.overlord.commons.services.ServiceRegistryUtil;

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
        Set<SAMLAssertionFactory> factories = null;
        
        // Note: use our classloader when loading the services because the application-specific
        // overlord-commons-auth-* implementations will likely be packaged up with the generic
        // overlord-commons-auth (this) module.  The exception being OSGi, which 
        // doesn't use ServiceLoader anyway.
        //
        // For example, when running in JBoss EAP 6.x, all of the overlord-commons-auth* JARs
        // are bundled up into a single JBoss Module.  In order for the ServiceLoader to work
        // properly, the context classloader would need to be set to the module's CL (so that
        // the service files are visible).
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SAMLAssertionUtil.class.getClassLoader());
        try {
            factories = ServiceRegistryUtil.getServices(SAMLAssertionFactory.class);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
        
        // Now that the factories are loaded, go ahead and try to use one of them.
        for (SAMLAssertionFactory factory : factories) {
            if (factory.accept()) {
                return factory.createSAMLAssertion(issuerName, forService);
            }
        }
        throw new RuntimeException("Unsupported/undetected platform.");
    }

}
