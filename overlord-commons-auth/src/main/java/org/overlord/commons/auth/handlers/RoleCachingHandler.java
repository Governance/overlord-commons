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

package org.overlord.commons.auth.handlers;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.handlers.saml2.BaseSAML2Handler;

/**
 * A SAML2 handler that simply saves the list of roles in the current
 * http session.
 */
public class RoleCachingHandler implements SAML2Handler {
    
    /**
     * C'tor.
     */
    public RoleCachingHandler() {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#initChainConfig(org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig)
     */
    @Override
    public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#initHandlerConfig(org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig)
     */
    @Override
    public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#reset()
     */
    @Override
    public void reset() throws ProcessingException {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#handleRequestType(org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest, org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse)
     */
    @Override
    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response)
            throws ProcessingException {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#handleStatusResponseType(org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest, org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse)
     */
    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response)
            throws ProcessingException {
        HttpSession session = BaseSAML2Handler.getHttpSession(request);
        if (session != null) {
            List<String> roles = response.getRoles();
            if (roles != null) {
                session.setAttribute(GeneralConstants.ROLES_ID, roles);
            }
        }
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#generateSAMLRequest(org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest, org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse)
     */
    @Override
    public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response)
            throws ProcessingException {
    }
    
    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler#getType()
     */
    @Override
    public HANDLER_TYPE getType() {
        return HANDLER_TYPE.SP;
    }
    

}
