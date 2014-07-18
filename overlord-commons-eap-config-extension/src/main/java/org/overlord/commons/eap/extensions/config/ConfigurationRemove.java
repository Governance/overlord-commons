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

package org.overlord.commons.eap.extensions.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * Remove a configuration
 *
 * @author Kevin Conner
 */
public class ConfigurationRemove extends AbstractRemoveStepHandler {

    static final ConfigurationRemove INSTANCE = new ConfigurationRemove();

    /**
     * @see org.jboss.as.controller.AbstractRemoveStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode)
     */
    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {
        final String name = model.get(Constants.ATTRIBUTE_NAME).asString();
        SubsystemLogger.ROOT_LOGGER.removingConfiguration(name);
        
        removeConfiguration(name);

        super.performRemove(context, operation, model);
    }

    /**
     * @param name
     */
    private void removeConfiguration(final String name) {
        try {
            InitialContext ctx = new InitialContext();
            ctx.unbind("java:/comp/env/overlord-config/" + name); //$NON-NLS-1$
        } catch (NamingException e) {
            // Don't worry about it.
        }
    }
}
