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

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author Kevin Conner
 */
public class DeploymentDefinition extends SimpleResourceDefinition {

    /**
     * Constructor.
     */
    protected DeploymentDefinition() {
        super(SubsystemExtension.PATH_DEPLOYMENT,
                SubsystemExtension.getResourceDescriptionResolver("deployment"), //$NON-NLS-1$
                DeploymentAdd.INSTANCE, DeploymentRemove.INSTANCE
        );
    }

    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder(Constants.ATTRIBUTE_NAME, ModelType.STRING, false)
                    .setXmlName(Attribute.NAME.getLocalName())
                    .setAllowExpression(false)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static final SimpleAttributeDefinition MODULE =
            new SimpleAttributeDefinitionBuilder(Constants.ATTRIBUTE_MODULE, ModelType.STRING, false)
                    .setXmlName(Attribute.MODULE.getLocalName())
                    .setAllowExpression(false)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static final SimpleAttributeDefinition VERSION =
            new SimpleAttributeDefinitionBuilder(Constants.ATTRIBUTE_VERSION, ModelType.STRING, false)
                    .setXmlName(Attribute.VERSION.getLocalName())
                    .setAllowExpression(false)
                    .setAllowNull(true)
                    .setValidator(new StringLengthValidator(1))
                    .build();
    
    protected static final SimpleAttributeDefinition[] ALL_ATTRIBUTES = {NAME, MODULE, VERSION};

    /**
     * @see org.jboss.as.controller.SimpleResourceDefinition#registerAttributes(org.jboss.as.controller.registry.ManagementResourceRegistration)
     */
    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(NAME, null, new ReloadRequiredWriteAttributeHandler(NAME));
        resourceRegistration.registerReadWriteAttribute(MODULE, null, new ReloadRequiredWriteAttributeHandler(MODULE));
        resourceRegistration.registerReadWriteAttribute(VERSION, null, new ReloadRequiredWriteAttributeHandler(VERSION));
    }
}
