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
package org.overlord.commons.dev.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Base class for dev servers.
 * @author eric.wittmann@redhat.com
 */
public abstract class DevServer {

    protected final String [] args;

    /**
     * Constructor.
     */
    public DevServer(String [] args) {
        this.args = args;
    }

    /**
     * Enables debug logging.
     */
    public void enableDebug() {
        System.setProperty("discovery-strategy.debug", "true");
    }

    /**
     * Disables debug logging.
     */
    public void disableDebug() {
        System.clearProperty("discovery-strategy.debug");
    }

    /**
     * Start/run the server.
     */
    public void go() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Development Server (" + getClass().getSimpleName() + ")");
        preConfig();
        DevServerEnvironment environment = createDevEnvironment();
        addModules(environment);

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(environment, handlers);

        environment.createAppConfigs();

        // Create the server.
        int serverPort = serverPort();
        Server server = new Server(serverPort);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms");

        postStart(environment);

        server.join();
    }

    /**
     * Do any configuration steps.
     */
    protected void preConfig() {
    }

    /**
     * @return the development environment to use for this run
     */
    protected abstract DevServerEnvironment createDevEnvironment();

    /**
     * Adds dev server modules.
     * @param environment
     */
    protected abstract void addModules(DevServerEnvironment environment);

    /**
     * Adds the dev server modules as contexts in jetty.
     * @param environment
     * @param handlers
     */
    protected abstract void addModulesToJetty(DevServerEnvironment environment, ContextHandlerCollection handlers) throws Exception;

    /**
     * Do any post startup tasks.
     */
    protected void postStart(DevServerEnvironment environment) throws Exception {
    }

    /**
     * @return the port to run the dev server on
     */
    protected int serverPort() {
        return 8080;
    }

}
