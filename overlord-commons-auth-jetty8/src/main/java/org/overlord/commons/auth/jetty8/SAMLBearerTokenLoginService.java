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

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.overlord.commons.auth.util.SAMLBearerTokenUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLAssertionParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Document;

/**
 * Extends the jetty JAAS login service to also support SAML Bearer Token authentication
 * in addition to simple BASIC authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLBearerTokenLoginService extends JAASLoginService {
    
    private static final Logger LOG = Log.getLogger(JAASLoginService.class);

    private Set<String> allowedIssuers;
    private boolean signatureRequired;
    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;

    /**
     * Constructor.
     */
    public SAMLBearerTokenLoginService() {
    }
    
    /**
     * @see org.eclipse.jetty.plus.jaas.JAASLoginService#login(java.lang.String, java.lang.Object)
     */
    @Override
    public UserIdentity login(String username, Object credentials) {
        String password = credentials.toString();
        if (password.startsWith("SAML-BEARER-TOKEN:")) {
            return doSamlLogin(username, password.substring(18));
        } else {
            return super.login(username, credentials);
        }
    }

    /**
     * Performs the SAML login by parsing the saml assertion and processing it.
     * @param username
     * @param samlAssertionData
     */
    private UserIdentity doSamlLogin(String username, String samlAssertionData) {
        try {
            Document samlAssertion = DocumentUtil.getDocument(samlAssertionData);
            SAMLAssertionParser parser = new SAMLAssertionParser();
            XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(new StringReader(samlAssertionData));
            Object parsed = parser.parse(xmlEventReader);
            AssertionType assertion = (AssertionType) parsed;
            SAMLBearerTokenUtil.validateAssertion(assertion, AbstractHttpConnection.getCurrentConnection().getRequest(), allowedIssuers);
            if (signatureRequired) {
                KeyPair keyPair = getKeyPair(assertion);
                if (!SAMLBearerTokenUtil.isSAMLAssertionSignatureValid(samlAssertion, keyPair)) {
                    throw new IOException("Invalid signature found on SAML assertion!");
                }
            }
            return consumeAssertion(assertion);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            LOG.debug(e);
        }
        return null;
    }

    /**
     * Gets the key pair to use to validate the assertion's signature.  The key pair is retrieved
     * from the keystore.
     * @param assertion
     * @throws IOException
     */
    private KeyPair getKeyPair(AssertionType assertion) throws IOException {
        KeyStore keystore = loadKeystore();
        try {
            return SAMLBearerTokenUtil.getKeyPair(keystore, keyAlias, keyPassword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to get KeyPair when validating SAML assertion signature.  Alias: " + keyAlias);
        }
    }

    /**
     * Loads the keystore.
     * @throws IOException
     */
    private KeyStore loadKeystore() throws IOException {
        try {
            return SAMLBearerTokenUtil.loadKeystore(keystorePath, keystorePassword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error loading signature keystore: " + e.getMessage());
        }
    }

    /**
     * Consumes the assertion, resulting in the extraction of the Subject as the
     * JAAS principal and the Role Statements as the JAAS roles.
     * @param assertion
     * @throws Exception
     */
    private UserIdentity consumeAssertion(AssertionType assertion) throws Exception {
        SubjectType samlSubjectType = assertion.getSubject();
        String samlSubject = ((NameIDType) samlSubjectType.getSubType().getBaseID()).getValue();

        List<String> roles = new ArrayList<String>();
        Set<StatementAbstractType> statements = assertion.getStatements();
        for (StatementAbstractType statement : statements) {
            if (statement instanceof AttributeStatementType) {
                AttributeStatementType attrStatement = (AttributeStatementType) statement;
                List<ASTChoiceType> attributes = attrStatement.getAttributes();
                for (ASTChoiceType astChoiceType : attributes) {
                    if (astChoiceType.getAttribute() != null && astChoiceType.getAttribute().getName().equals("Role")) {
                        List<Object> values = astChoiceType.getAttribute().getAttributeValue();
                        for (Object roleValue : values) {
                            if (roleValue != null) {
                                roles.add(roleValue.toString());
                            }
                        }
                    }
                }
            }
        }

        Subject subject = new Subject();
        Principal principal = new SAMLUserPrincipal(samlSubject);
        subject.getPrincipals().add(principal);
        for (String role : roles) {
            subject.getPrincipals().add(new SAMLRolePrincipal(role));
        }
        return _identityService.newUserIdentity(subject, principal, roles.toArray(new String[roles.size()]));
    }
    
    /**
     * User Principal class.
     */
    private static class SAMLUserPrincipal implements Principal {
        
        private final String name;
        
        /**
         * Constructor.
         */
        public SAMLUserPrincipal(String name) {
            this.name = name;
        }

        /**
         * @see java.security.Principal#getName()
         */
        @Override
        public String getName() {
            return name;
        }
    }

    /**
     * Role Principal class.
     */
    private static class SAMLRolePrincipal extends SAMLUserPrincipal {

        /**
         * Constructor.
         */
        public SAMLRolePrincipal(String name) {
            super(name);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SAMLRolePrincipal))
                return false;
            return getName().equals(((SAMLRolePrincipal) o).getName());
        }
    }

    /**
     * @return the allowedIssuers
     */
    public Set<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    /**
     * @param allowedIssuers the allowedIssuers to set
     */
    public void setAllowedIssuers(Set<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }

    /**
     * @param allowedIssuers the allowedIssuers to set
     */
    public void setAllowedIssuers(String[] allowedIssuers) {
        this.allowedIssuers = new HashSet<String>();
        for (String issuer : allowedIssuers) {
            this.allowedIssuers.add(issuer);
        }
    }

    /**
     * @return the signatureRequired
     */
    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    /**
     * @param signatureRequired the signatureRequired to set
     */
    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    /**
     * @return the keystorePath
     */
    public String getKeystorePath() {
        return keystorePath;
    }

    /**
     * @param keystorePath the keystorePath to set
     */
    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    /**
     * @return the keystorePassword
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * @param keystorePassword the keystorePassword to set
     */
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    /**
     * @return the keyAlias
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * @param keyAlias the keyAlias to set
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * @return the keyPassword
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * @param keyPassword the keyPassword to set
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

}
