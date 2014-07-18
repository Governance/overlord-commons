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
public class ConfigurationPropertyDefinition extends SimpleResourceDefinition {

    protected ConfigurationPropertyDefinition() {
        super(SubsystemExtension.PATH_PROPERTY,
                SubsystemExtension.getResourceDescriptionResolver("configuration.property"), //$NON-NLS-1$
                ConfigurationPropertyAdd.INSTANCE, null
        );
    }

    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder(Constants.ATTRIBUTE_NAME, ModelType.STRING, false)
                    .setXmlName(Attribute.NAME.getLocalName())
                    .setAllowExpression(false)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static final SimpleAttributeDefinition VALUE =
            new SimpleAttributeDefinitionBuilder(Constants.ATTRIBUTE_VALUE, ModelType.STRING, false)
                    .setXmlName(Attribute.VALUE.getLocalName())
                    .setAllowExpression(false)
                    .build();
    protected static final SimpleAttributeDefinition[] ALL_ATTRIBUTES = {NAME, VALUE};

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(NAME, null, new ReloadRequiredWriteAttributeHandler(NAME));
        resourceRegistration.registerReadWriteAttribute(VALUE, null, new ReloadRequiredWriteAttributeHandler(VALUE));
    }
}
