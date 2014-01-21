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

package org.overlord.commons.auth.filters;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.commons.codec.binary.Base64;
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
 * A filter that supports both BASIC authentication and custom SAML Bearer Token
 * authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class SamlBearerTokenAuthFilter implements Filter {

    private String realm;
    private Set<String> allowedIssuers;
    private boolean signatureRequired;
    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;

    /**
     * Constructor.
     */
    public SamlBearerTokenAuthFilter() {
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // Realm
        String parameter = config.getInitParameter("realm");
        if (parameter != null && parameter.trim().length() > 0) {
            realm = parameter;
        } else {
            realm = defaultRealm();
        }
        
        // Allowed issuers
        parameter = config.getInitParameter("allowedIssuers");
        if (parameter != null && parameter.trim().length() > 0) {
            allowedIssuers = new HashSet<String>();
            String [] split = parameter.split(",");
            for (String issuer : split) {
                allowedIssuers.add(issuer);
            }
        } else {
            allowedIssuers = defaultAllowedIssuers();
        }

        // Signature Required
        parameter = config.getInitParameter("signatureRequired");
        if (parameter != null && parameter.trim().length() > 0) {
            signatureRequired = Boolean.parseBoolean(parameter);
        } else {
            signatureRequired = defaultSignatureRequired();
        }

        // Keystore Path
        parameter = config.getInitParameter("keystorePath");
        if (parameter != null && parameter.trim().length() > 0) {
            keystorePath = parameter;
        } else {
            keystorePath = defaultKeystorePath();
        }

        // Keystore Password
        parameter = config.getInitParameter("keystorePassword");
        if (parameter != null && parameter.trim().length() > 0) {
            keystorePassword = parameter;
        } else {
            keystorePassword = defaultKeystorePassword();
        }

        // Key alias
        parameter = config.getInitParameter("keyAlias");
        if (parameter != null && parameter.trim().length() > 0) {
            keyAlias = parameter;
        } else {
            keyAlias = defaultKeyAlias();
        }

        // Key Password
        parameter = config.getInitParameter("keyPassword");
        if (parameter != null && parameter.trim().length() > 0) {
            keyPassword = parameter;
        } else {
            keyPassword = defaultKeyPassword();
        }

    }

    /**
     * @return the default keystore password
     */
    protected String defaultKeystorePassword() {
        return null;
    }

    /**
     * @return the default key alias
     */
    protected String defaultKeyAlias() {
        return null;
    }

    /**
     * @return the default key password
     */
    protected String defaultKeyPassword() {
        return null;
    }

    /**
     * @return the default value of keystorePath
     */
    protected String defaultKeystorePath() {
        return null;
    }

    /**
     * @return the default value of signatureRequired
     */
    protected boolean defaultSignatureRequired() {
        return false;
    }

    /**
     * @return the default set of allowed issuers
     */
    protected Set<String> defaultAllowedIssuers() {
        return Collections.<String>emptySet();
    }

    /**
     * @return the default realm
     */
    protected String defaultRealm() {
        return "Overlord";
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");
        Creds credentials = parseAuthorizationHeader(authHeader);
        if  (credentials == null) {
            sendAuthResponse((HttpServletResponse)response);
            return;
        }
        
        SimplePrincipal principal = login(credentials, req, (HttpServletResponse) response);
        if (principal != null) {
            doFilterChain(request, response, chain, principal);
        } else {
            sendAuthResponse((HttpServletResponse)response);
        }
    }

    /**
     * Further process the filter chain.
     * @param request
     * @param response
     * @param chain
     * @param principal
     * @throws IOException
     * @throws ServletException
     */
    protected void doFilterChain(ServletRequest request, ServletResponse response, FilterChain chain,
            SimplePrincipal principal) throws IOException, ServletException {
        chain.doFilter(proxyRequest(request, principal), response);
    }

    /**
     * Wrap/proxy the http request.
     * @param request
     * @param principal
     */
    private HttpServletRequest proxyRequest(final ServletRequest request, final SimplePrincipal principal) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getUserPrincipal")) {
                    return principal;
                } else if (method.getName().equals("getRemoteUser")) {
                    return principal.getName();
                } else if (method.getName().equals("isUserInRole")) {
                    String role = (String) args[0];
                    return principal.getRoles().contains(role);
                }
                return method.invoke(request, args);
            }
        };
        return (HttpServletRequest) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] { HttpServletRequest.class }, handler);
    }

    /**
     * Parses the Authorization request header into a username and password.
     * @param authHeader
     */
    private Creds parseAuthorizationHeader(String authHeader) {
        if (authHeader == null)
            return null;
        if (!authHeader.toUpperCase().startsWith("BASIC "))
            return null;

        try {
            String userpassEncoded = authHeader.substring(6);
            byte[] decoded = Base64.decodeBase64(userpassEncoded);
            String data = new String(decoded, "UTF-8");
            int sepIdx = data.indexOf(':');
            if (sepIdx > 0) {
                String username = data.substring(0, sepIdx);
                String password = data.substring(sepIdx + 1);
                return new Creds(username, password);
            } else {
                return new Creds(data, null);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the basic authentication credentials.
     * @param credentials
     * @param request
     * @param response
     * @throws IOException 
     */
    protected SimplePrincipal login(Creds credentials, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if ("SAML-BEARER-TOKEN".equals(credentials.username)) {
            return doSamlLogin(credentials.password, request);
        } else {
            return doBasicLogin(credentials.username, credentials.password, request);
        }
    }

    /**
     * Handles SAML Bearer token authentication.  Assumes the password is an
     * encoded SAML assertion.
     * @param assertionData
     * @param request
     * @throws IOException 
     */
    protected SimplePrincipal doSamlLogin(String assertionData, HttpServletRequest request) throws IOException {
        try {
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
            return consumeAssertion(assertion);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    private SimplePrincipal consumeAssertion(AssertionType assertion) throws Exception {
        SubjectType samlSubjectType = assertion.getSubject();
        String samlSubject = ((NameIDType) samlSubjectType.getSubType().getBaseID()).getValue();

        SimplePrincipal identity = new SimplePrincipal(samlSubject);

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
                                identity.addRole(roleValue.toString());
                            }
                        }
                    }
                }
            }
        }

        return identity;
    }

    /**
     * Fall back to standard basic authentication.  Subclasses must implement this
     * method.
     * @param username
     * @param password
     * @param request 
     * @throws IOException
     */
    protected abstract SimplePrincipal doBasicLogin(String username, String password, HttpServletRequest request) throws IOException;

    /**
     * Sends a response that tells the client that authentication is required.
     * @param response
     * @throws IOException 
     */
    private void sendAuthResponse(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", String.format("BASIC realm=\"%1$s\"", realm));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
    
    /**
     * Models inbound basic auth credentials (user/password).
     * @author eric.wittmann@redhat.com
     */
    protected static class Creds {
        public String username;
        public String password;
        
        /**
         * Constructor.
         */
        public Creds(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
