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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * Extension for a subsystem that can deploy other artifacts through the module hierarchy.
 */
public class SubsystemExtension implements Extension {
    public static final String SUBSYSTEM_NAME = "overlord-deployment"; //$NON-NLS-1$

    public static final PathElement PATH_SUBSYSTEM = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    public static final PathElement PATH_DEPLOYMENT = PathElement.pathElement(Constants.MODEL_DEPLOYMENT);

    private static final String RESOURCE_NAME = SubsystemExtension.class.getPackage().getName() + ".LocalDescriptions"; //$NON-NLS-1$

    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;
    private static final int MICRO_VERSION = 0;

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, SubsystemExtension.class.getClassLoader(), true, false);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final ExtensionContext context) {
        SubsystemLogger.ROOT_LOGGER.activatingExtension();
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, MAJOR_VERSION, MINOR_VERSION, MICRO_VERSION);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(new SubsystemDefinition());
        registration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
        registration.registerSubModel(new DeploymentDefinition());
        
        subsystem.registerXMLElementWriter(SubsystemParser_1_0.INSTANCE);
    }

    /** {@inheritDoc} */
    @Override
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, Namespace.OVERLORD_DEPLOYMENT_1_0.getUriString(), SubsystemParser_1_0.INSTANCE);
    }
}
