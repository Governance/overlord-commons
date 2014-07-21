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

import java.net.URL;

import org.jboss.dmr.ModelNode;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * This file is using the subset 1000-1100 for logger messages.
 */
@MessageLogger(projectCode = "OVERLORD")
public interface SubsystemLogger extends BasicLogger {

    /**
     * A logger with a category of the package name.
     */
    public SubsystemLogger ROOT_LOGGER = Logger.getMessageLogger(SubsystemLogger.class, "org.overlord.commons.eap.extensions.deploy"); //$NON-NLS-1$

    /**
     * Logs a debug message with details of the current deployment.
     *
     *@param name The deployment name
     *@param moduleName The module nem
     *@param moduleVersion the module version
     *@param type The deployment type
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1000, value = "Processing deployment name: %1$s, module name: %2$s, module version: %3$s")
    public void deploymentInformation(final String name, final String moduleName, final ModelNode moduleVersion);

    /**
     * Debug message detailing the module identifier being loaded.
     * 
     * @param moduleIdentifier The module identifier
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1001, value = "Loading module %s")
    public void locatingModule(final ModuleIdentifier moduleIdentifier);

    /**
     * Error message detailing the module identifier that could not be loaded.
     * 
     * @param mle The module load exception causing the failure
     * @param moduleIdentifier The module identifier
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = 1002, value = "Failed to load module identifier %s")
    public void failedToLoadModule(final @Cause ModuleLoadException mle, final ModuleIdentifier moduleIdentifier);

    /**
     * Debug message detailing the successful loading of the module.
     * 
     * @param moduleIdentifier The module identifier
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1003, value = "Module %s has been loaded")
    public void loadModule(ModuleIdentifier moduleIdentifier);
    
    /**
     * Debug message detailing the URL of the real deployment.
     * 
     * @param url The URL of the deployment
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1004, value = "Deployment URL %s")
    public void deploymentURL(final URL url);
    
    /**
     * Info message documenting the start of the extension.
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 1005, value = "Activating Overlord deployment Extension")
    public void activatingExtension();

    /**
     * Logs a debug message with details of the deployment being removed.
     *
     *@param name The deployment name
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 1006, value = "Removing deployment %s")
    public void removingDeployment(final String name);
}
