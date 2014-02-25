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

/**
 * Factory for creating SAML assertions.  Useful when invoking REST services protected
 * by SAML Bearer Token authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public interface SAMLAssertionFactory {
    
    /**
     * Returns true if the instance of the assertion factory should be used in the 
     * current environment.  The implementation is expected to be able to determine
     * this.
     */
    public boolean accept();
    
    /**
     * Creates a SAML Assertion that can be used as a bearer token when invoking
     * REST services. The REST service must be configured to accept SAML
     * Assertion bearer tokens.
     * @param issuerName
     * @param forService
     */
    public String createSAMLAssertion(String issuerName, String forService, int timeValidInMillis);

}
