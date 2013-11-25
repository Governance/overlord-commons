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

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
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
 * A BASIC authenticator that handles inbound SAML Bearer Token authentication.  This 
 * authenticator assumes 
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLBearerTokenAuthenticator extends BasicAuthenticator {
    
    private Set<String> allowedIssuers;
    private boolean signatureRequired;
    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;

    /**
     * Constructor.
     */
    public SAMLBearerTokenAuthenticator() {
    }
    
    /**
     * @see org.apache.catalina.authenticator.BasicAuthenticator#authenticate(org.apache.catalina.connector.Request, javax.servlet.http.HttpServletResponse, org.apache.catalina.deploy.LoginConfig)
     */
    @Override
    public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config)
            throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("authorization");
            if (authorization != null) {
                authorization.toBytes();
                ByteChunk authorizationBC = authorization.getByteChunk();
                if (authorizationBC.startsWithIgnoreCase("basic ", 0)) {
                    authorizationBC.setOffset(authorizationBC.getOffset() + 6);
                    String b64Data = new String(authorizationBC.getBuffer(), authorizationBC.getOffset(),
                            authorizationBC.getLength());
                    byte[] decoded = Base64.decodeBase64(b64Data);
                    String data = new String(decoded, "UTF-8");
                    if (data.startsWith("SAML-BEARER-TOKEN:")) {
                        try {
                            String assertionData = data.substring(18);
                            Document samlAssertion = DocumentUtil.getDocument(assertionData);
                            SAMLAssertionParser parser = new SAMLAssertionParser();
                            XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(new StringReader(assertionData));
                            Object parsed = parser.parse(xmlEventReader);
                            AssertionType assertion = (AssertionType) parsed;
                            SAMLBearerTokenUtil.validateAssertion(assertion, request, allowedIssuers);
                            if (signatureRequired) {
                                KeyPair keyPair = getKeyPair(assertion);
                                if (!SAMLBearerTokenUtil.isSAMLAssertionSignatureValid(samlAssertion, keyPair)) {
                                    throw new IOException("Invalid signature found on SAML assertion!");
                                }
                            }
                            principal = consumeAssertion(assertion);
                            if (principal != null) {
                                register(request, response, principal,
                                        HttpServletRequest.BASIC_AUTH, principal.getName(), null);
                                return true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
                authorizationBC.setOffset(authorizationBC.getOffset() - 6);
            }
        }
        return super.authenticate(request, response, config);
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
    private Principal consumeAssertion(AssertionType assertion) throws Exception {
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

        Principal identity = new GenericPrincipal(samlSubject, "", roles);
        return identity;
    }

    /**
     * @return the allowedIssuers
     */
    public String getAllowedIssuers() {
        return allowedIssuers.toString();
    }

    /**
     * @param allowedIssuers the allowedIssuers to set
     */
    public void setAllowedIssuers(String allowedIssuers) {
        allowedIssuers = interpolate(allowedIssuers);
        if (this.allowedIssuers == null) {
            this.allowedIssuers = new HashSet<String>();
        }
        this.allowedIssuers.clear();
        if (allowedIssuers != null) {
            String[] issuers = allowedIssuers.split(",");
            for (String issuer : issuers) {
                this.allowedIssuers.add(issuer.trim());
            }
        }
    }

    /**
     * @return the signatureRequired
     */
    public String getSignatureRequired() {
        return String.valueOf(signatureRequired);
    }

    /**
     * @param signatureRequired the signatureRequired to set
     */
    public void setSignatureRequired(String signatureRequired) {
        signatureRequired = interpolate(signatureRequired);
        this.signatureRequired = Boolean.valueOf(signatureRequired);
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
        keystorePath = interpolate(keystorePath);
        // Handle relative path - always relative to catalina.home
        if (keystorePath != null && !keystorePath.startsWith("/") && keystorePath.charAt(2) != ':') {
            String home = System.getProperty("catalina.home");
            if (home != null) {
                keystorePath = home + "/" + keystorePath;
            }
        }
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
        keystorePassword = interpolate(keystorePassword);
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
        keyAlias = interpolate(keyAlias);
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
        keyPassword = interpolate(keyPassword);
        this.keyPassword = keyPassword;
    }
    
    /**
     * Do system property interpolation.
     * @param value
     */
    private String interpolate(String value) {
        if (value != null && value.startsWith("${")) {
            int idx = value.indexOf("::");
            if (idx < 3) {
                return value;
            }
            String propName = value.substring(2, idx);
            String defaultValue = value.substring(idx + 2, value.length()-1);
            return System.getProperty(propName, defaultValue);
        } else {
            return value;
        }
    }

}
