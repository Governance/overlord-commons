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

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * This file is using the subset 1100-1200 for logger messages.
 */
@MessageBundle(projectCode = "OVERLORD")
public interface SubsystemMessages {

    /**
     * The messages
     */
    public SubsystemMessages MESSAGES = Messages.getBundle(SubsystemMessages.class);
    
    /**
     * Create the exception representing the failure to deploy the module.
     *
     * @param mle The module load exception causing the failure
     * @param moduleIdentifier The module identifier
     * 
     * @return the operation failed exception.
     */
    @Message(id = 1100, value = "Failed to load module %s.")
    public OperationFailedException failedToLoadModule(final @Cause ModuleLoadException mle, final ModuleIdentifier moduleIdentifier);

    /**
     * Create the exception detailing the failure to locate the deployment.
     *
     * @param moduleIdentifier The module identifier
     * @param name The name of the deployment
     * 
     * @return the operation failed exception.
     */
    @Message(id = 1101, value = "Could not locate deployment %2$s within module %1$s.")
    public OperationFailedException couldNotLocateDeployment(final ModuleIdentifier moduleIdentifier, final String name);

}
