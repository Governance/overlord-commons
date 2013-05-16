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

import org.eclipse.jetty.server.handler.ContextHandlerCollection;



/**
 * Base class for errai dev servers.
 * @author eric.wittmann@redhat.com
 */
public abstract class ErraiDevServer extends DevServer {

    /**
     * Constructor.
     */
    public ErraiDevServer(String [] args) {
        super(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModulesToJetty(org.overlord.commons.dev.server.DevServerEnvironment, org.eclipse.jetty.server.handler.ContextHandlerCollection)
     */
    @Override
    protected void addModulesToJetty(DevServerEnvironment environment, ContextHandlerCollection handlers)
            throws Exception {
        if (environment.isModuleInIDE(getErraiModuleId()) && !environment.isUsingClassHiderAgent()) {
            System.out.println("******************************************************************");
            System.out.println("WARNING: we detected that you are running from within an IDE");
            System.out.println("         but are not using the Errai class hiding agent.  As");
            System.out.println("         a result, you may see a number of Weld related errors ");
            System.out.println("         during startup.  This is due to client-only classes");
            System.out.println("         being included on the server classpath.  To address");
            System.out.println("         this issue, please see:");
            System.out.println("         ");
            System.out.println("         https://github.com/jfuerth/client-local-class-hider");
            System.out.println("         ");
            System.out.println("         The above is a Java Agent that will hide the client-");
            System.out.println("         only classes from Weld, thereby suppressing the errors.");
            System.out.println("******************************************************************");
            try {Thread.sleep(5000);} catch (InterruptedException e) {}
        }

    }

    protected abstract String getErraiModuleId();

}
