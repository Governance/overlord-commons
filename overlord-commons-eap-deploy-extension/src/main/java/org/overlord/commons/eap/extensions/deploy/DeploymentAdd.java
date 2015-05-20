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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ARCHIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PERSISTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.URL;

import java.io.File;
import java.net.URL;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * Add a deployment
 *
 * @author Kevin Conner
 */
public class DeploymentAdd extends AbstractAddStepHandler {

    static final DeploymentAdd INSTANCE = new DeploymentAdd();
    
    /**
     * Constructor.
     */
    public DeploymentAdd() {
    }

    /**
     * @see org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode)
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    }

    /**
     * @see org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.as.controller.registry.Resource)
     */
    @Override
    protected void populateModel(final OperationContext context, final ModelNode operation, final Resource resource) throws  OperationFailedException {
        final ModelNode model = resource.getModel();
        
        model.setEmptyObject();
        for (SimpleAttributeDefinition attr : DeploymentDefinition.ALL_ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
        
        if (requiresRuntime(context)) {
            final String name = model.get(Constants.ATTRIBUTE_NAME).asString();
            final String moduleName = model.get(Constants.ATTRIBUTE_MODULE).asString();
            final ModelNode versionNode = model.get(Constants.ATTRIBUTE_VERSION);
            final String moduleVersion = (versionNode.isDefined() ? versionNode.asString() : null);
    
            SubsystemLogger.ROOT_LOGGER.deploymentInformation(name, moduleName, versionNode);
            
            final ModuleIdentifier moduleIdentifier = ModuleIdentifier.create(moduleName, moduleVersion);
            SubsystemLogger.ROOT_LOGGER.locatingModule(moduleIdentifier);
            try {
                final Module module = Module.getBootModuleLoader().loadModule(moduleIdentifier);
                SubsystemLogger.ROOT_LOGGER.loadModule(moduleIdentifier);
                
                final PathAddress deploymentAddress = PathAddress.pathAddress(PathElement.pathElement(DEPLOYMENT, name));
                final ModelNode op = Util.createOperation(ADD, deploymentAddress);
                op.get(ENABLED).set(true);
                op.get(PERSISTENT).set(false);
    
                final URL url = module.getExportedResource(name);
                if (url == null) {
                    throw SubsystemMessages.MESSAGES.couldNotLocateDeployment(moduleIdentifier, name);
                }
                
                File deployment = new File(url.toURI());
                
                SubsystemLogger.ROOT_LOGGER.deploymentURL(url);
                
                final ModelNode contentItem = new ModelNode();
                
                if (deployment.isDirectory()) {
                    // an exploded deployment
                    contentItem.get(PATH).set(deployment.getAbsolutePath());
                    contentItem.get(ARCHIVE).set(false);
                } else {
                    // an unexploded deployment archive
                    contentItem.get(URL).set(url.toExternalForm());
                }
    
                op.get(CONTENT).add(contentItem);
    
                final ImmutableManagementResourceRegistration rootResourceRegistration = context.getRootResourceRegistration();
                final OperationStepHandler handler = rootResourceRegistration.getOperationHandler(deploymentAddress, ADD);
    
                context.addStep(op, handler, OperationContext.Stage.MODEL);
                
                context.stepCompleted();
            } catch (final ModuleLoadException mle) {
                SubsystemLogger.ROOT_LOGGER.failedToLoadModule(mle, moduleIdentifier);
                throw SubsystemMessages.MESSAGES.failedToLoadModule(mle, moduleIdentifier);
            } catch (Exception e) {
                throw new OperationFailedException("Deployments failed", e);
            }
        }
    }
    
    /**
     * @see org.jboss.as.controller.AbstractAddStepHandler#requiresRuntimeVerification()
     */
    @Override
    protected boolean requiresRuntimeVerification() {
        return false;
    }
}
