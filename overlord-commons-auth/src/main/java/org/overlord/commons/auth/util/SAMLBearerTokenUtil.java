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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Document;

/**
 * Class used to create SAML bearer tokens used when calling REST service
 * endpoints protected by SAML.
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLBearerTokenUtil {

    /**
     * Creates a SAML Assertion that can be used as a bearer token when invoking REST
     * services.  The REST service must be configured to accept SAML Assertion bearer
     * tokens.
     * 
     * In JBoss this means protecting the REST services with {@link SAMLBearerTokenLoginModule}.
     * In Tomcat7 this means protecting the REST services with {@link SAMLBearerTokenAuthenticator}.
     * 
     * @param principal the authenticated principal
     * @param roles the authenticated principal's roles
     * @param issuerName the issuer name (typically the context of the calling web app)
     * @param forService the web context of the REST service being invoked
     */
    public static String createSAMLAssertion(Principal principal, Set<String> roles, String issuerName, String forService) {
        try {
            NameIDType issuer = SAMLAssertionFactory.createNameID(null, null, issuerName);
            SubjectType subject = AssertionUtil.createAssertionSubject(principal.getName());
            AssertionType assertion = AssertionUtil.createAssertion(UUID.randomUUID().toString(), issuer);
            assertion.setSubject(subject);
            AssertionUtil.createTimedConditions(assertion, 10000);
            ConditionAbstractType restriction = SAMLAssertionFactory.createAudienceRestriction(forService);
            assertion.getConditions().addCondition(restriction);
            addRoleStatements(roles, assertion, principal);

            return AssertionUtil.asString(assertion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add the user's current roles as attribute statement(s) on the SAML Assertion.
     * @param roles
     * @param assertion
     * @param principal
     */
    private static void addRoleStatements(Set<String> roles, AssertionType assertion, Principal principal) {
        AttributeType attribute = new AttributeType("Role");
        ASTChoiceType attributeAST = new ASTChoiceType(attribute);
        AttributeStatementType roleStatement = new AttributeStatementType();
        roleStatement.addAttribute(attributeAST);
        
        if (roles != null) {
            for (String role : roles) {
                attribute.addAttributeValue(role);
            }
        }

        assertion.addStatement(roleStatement);
    }

    /**
     * Signs a SAML assertion using the given security {@link KeyPair}.
     * @param assertion
     * @param keypair
     */
    public static String signSAMLAssertion(String assertion, KeyPair keypair) {
        try {
            Document samlDocument = DocumentUtil.getDocument(assertion);
            SAML2Signature sig = new SAML2Signature();
            sig.signSAMLDocument(samlDocument, keypair);
            return DocumentUtil.getDocumentAsString(samlDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the SAML assertion's signature is valid.
     */
    public static boolean isSAMLAssertionSignatureValid(Document samlAssertion, KeyPair keyPair) {
        return AssertionUtil.isSignatureValid(samlAssertion.getDocumentElement(), keyPair.getPublic());
    }

    /**
     * Gets the key pair to use to either sign an assertion or validate an assertion's signature. The key pair
     * is retrieved from the keystore.
     */
    public static KeyPair getKeyPair(KeyStore keystore, String keyAlias, String keyPassword) throws Exception {
        try {
            Key key = keystore.getKey(keyAlias, keyPassword.toCharArray());
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(keyAlias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
            throw new Exception("Failed to get KeyPair from KeyStore.  Incorrect key type found for alias: " + keyAlias);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to get KeyPair from KeyStore.  Alias: " + keyAlias);
        }
    }

    /**
     * Loads the keystore.
     * @param keystorePath
     * @param keystorePassword
     * @throws Exception
     */
    public static KeyStore loadKeystore(String keystorePath, String keystorePassword) throws Exception {
        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.isFile()) {
            throw new Exception("No KeyStore found at path " + keystorePath);
        }
        KeyStore keystore = KeyStore.getInstance("jks");
        InputStream is = null;
        try {
            is = new FileInputStream(keystoreFile);
            keystore.load(is, keystorePassword.toCharArray());
            return keystore;
        } finally {
            if (is != null) { try { is.close(); } catch (Exception e) {} }
        }
    }
    
    /**
     * Validates that the assertion is acceptable based on configurable criteria.
     * @param assertion
     * @param request
     * @param allowedIssuers
     * @throws LoginException
     */
    public static void validateAssertion(AssertionType assertion, HttpServletRequest request, Set<String> allowedIssuers) throws LoginException {
        // Possibly fail the assertion based on issuer.
        String issuer = assertion.getIssuer().getValue();
        if (allowedIssuers != null && !allowedIssuers.contains(issuer)) {
            throw new LoginException("Dis-allowed SAML Assertion Issuer: " + issuer + " Allowed: " + allowedIssuers);
        }

        // Possibly fail the assertion based on audience restriction
        String currentAudience = request.getContextPath();
        Set<String> audienceRestrictions = getAudienceRestrictions(assertion);
        if (!audienceRestrictions.contains(currentAudience)) {
            throw new LoginException("SAML Assertion Audience Restrictions not valid for this context ("
                    + currentAudience + ")");
        }

        // Possibly fail the assertion based on time.
        try {
            ConditionsType conditionsType = assertion.getConditions();
            if (conditionsType != null) {
                XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
                XMLGregorianCalendar notBefore = conditionsType.getNotBefore();
                XMLGregorianCalendar notOnOrAfter = conditionsType.getNotOnOrAfter();
                if (!XMLTimeUtil.isValid(now, notBefore, notOnOrAfter)) {
                    String msg = "SAML Assertion has expired: " +
                            "Now=" + now.toXMLFormat() + " ::notBefore=" + notBefore.toXMLFormat() + " ::notOnOrAfter=" + notOnOrAfter;
                    throw new LoginException(msg);
                }
            } else {
                throw new LoginException("SAML Assertion not valid (no Conditions supplied).");
            }
        } catch (ConfigurationException e) {
            // should never happen - see AssertionUtil.hasExpired code for why
            throw new LoginException(e.getMessage());
        }
    }

    /**
     * Gets the audience restriction condition.
     * @param assertion
     */
    private static Set<String> getAudienceRestrictions(AssertionType assertion) {
        Set<String> rval = new HashSet<String>();
        if (assertion == null || assertion.getConditions() == null || assertion.getConditions().getConditions() == null)
            return rval;

        List<ConditionAbstractType> conditions = assertion.getConditions().getConditions();
        for (ConditionAbstractType conditionAbstractType : conditions) {
            if (conditionAbstractType instanceof AudienceRestrictionType) {
                AudienceRestrictionType art = (AudienceRestrictionType) conditionAbstractType;
                List<URI> audiences = art.getAudience();
                for (URI uri : audiences) {
                    rval.add(uri.toString());
                }
            }
        }

        return rval;
    }
}
