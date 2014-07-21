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

package org.overlord.commons.eap.extensions.deploy;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

/**
 * Remove a deployment
 *
 * @author Kevin Conner
 */
public class DeploymentRemove extends AbstractRemoveStepHandler {

    static final DeploymentRemove INSTANCE = new DeploymentRemove();

    @Override
    protected void performRemove(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {
        final String name = model.get(Constants.ATTRIBUTE_NAME).asString();
        SubsystemLogger.ROOT_LOGGER.removingDeployment(name);
        
        if (requiresRuntime(context)) {
            final PathAddress deploymentAddress = PathAddress.pathAddress(PathElement.pathElement(DEPLOYMENT, name));
            final ModelNode op = Util.createOperation(REMOVE, deploymentAddress);
    
            final ImmutableManagementResourceRegistration rootResourceRegistration = context.getRootResourceRegistration();
            final OperationStepHandler handler = rootResourceRegistration.getOperationHandler(deploymentAddress, REMOVE);
    
            context.addStep(op, handler, OperationContext.Stage.MODEL);
        }

        super.performRemove(context, operation, model);
    }
}
