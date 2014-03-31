/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.commons.dev.server.discovery;

/**
 * Base class for strategies.
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractModuleDiscoveryStrategy implements IModuleDiscoveryStrategy {

    /**
     * Constructor.
     */
    public AbstractModuleDiscoveryStrategy() {
    }

    /**
     * @param message
     */
    protected void debug(String message) {
        if ("true".equals(System.getProperty("discovery-strategy.debug"))) //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(getClass().getSimpleName() + ": " + message); //$NON-NLS-1$
    }

}
