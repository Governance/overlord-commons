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

import java.util.Map;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * This file is using the subset 2000-2100 for logger messages.
 */
@MessageLogger(projectCode = "OVERLORD")
public interface SubsystemLogger extends BasicLogger {

    /**
     * A logger with a category of the package name.
     */
    public SubsystemLogger ROOT_LOGGER = Logger.getMessageLogger(SubsystemLogger.class, "org.overlord.commons.eap.extensions.config"); //$NON-NLS-1$

    /**
     * Logs a debug message with details of the current configuration.
     *
     *@param name The configuration name
     * @param propertyMap 
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 2000, value = "Processing configuration name: %1$s, properties: %2$s")
    public void configurationInformation(final String name, final Map<String, String> propertyMap);

    /**
     * Info message documenting the start of the extension.
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 2001, value = "Activating Overlord Configuration Extension")
    public void activatingExtension();

    /**
     * Logs a debug message with details of the configuration being removed.
     *
     *@param name The configuration name
     */
    @LogMessage(level = Level.DEBUG)
    @Message(id = 2002, value = "Removing configuration %s")
    public void removingConfiguration(final String name);
}
