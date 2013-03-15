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
package org.overlord.commons.auth.jboss7;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

import org.jboss.security.SecurityContextAssociation;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;

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
     * tokens (in JBoss this means protecting the REST services with {@link SAMLBearerTokenLoginModule}.
     * @param issuerName the issuer name (typically the context of the calling web app)
     * @param forService the web context of the REST service being invoked
     */
    public static String createSAMLAssertion(String issuerName, String forService) {
        try {
            Principal principal = SecurityContextAssociation.getPrincipal();
            NameIDType issuer = SAMLAssertionFactory.createNameID(null, null, issuerName);
            SubjectType subject = AssertionUtil.createAssertionSubject(principal.getName());
            AssertionType assertion = AssertionUtil.createAssertion(UUID.randomUUID().toString(), issuer);
            assertion.setSubject(subject);
            AssertionUtil.createTimedConditions(assertion, 10000);
            ConditionAbstractType restriction = SAMLAssertionFactory.createAudienceRestriction(forService);
            assertion.getConditions().addCondition(restriction);
            addRoleStatements(assertion, principal);

            return AssertionUtil.asString(assertion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add the user's current roles as attribute statement(s) on the SAML Assertion.
     * @param assertion
     * @param principal
     */
    private static void addRoleStatements(AssertionType assertion, Principal principal) {
        AttributeType attribute = new AttributeType("Role");
        ASTChoiceType attributeAST = new ASTChoiceType(attribute);
        AttributeStatementType roleStatement = new AttributeStatementType();
        roleStatement.addAttribute(attributeAST);

        Set<Principal> userRoles = SecurityContextAssociation.getSecurityContext().getAuthorizationManager().getUserRoles(principal);
        if (userRoles != null) {
            for (Principal role : userRoles) {
                attribute.addAttributeValue(role.getName());
            }
        }

        assertion.addStatement(roleStatement);
    }


}
