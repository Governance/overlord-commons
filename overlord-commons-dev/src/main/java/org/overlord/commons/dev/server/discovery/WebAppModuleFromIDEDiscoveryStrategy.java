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

import java.io.File;

import org.overlord.commons.dev.server.DevServerModule;

/**
 * Discovers a web app module when it's in the IDE.
 * @author eric.wittmann@redhat.com
 */
public class WebAppModuleFromIDEDiscoveryStrategy implements IModuleDiscoveryStrategy {

    private final Class<?> someClass;

    /**
     * Constructor.
     *
     * @param someClass
     */
    public WebAppModuleFromIDEDiscoveryStrategy(Class<?> someClass) {
        this.someClass = someClass;
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#getName()
     */
    @Override
    public String getName() {
        return "IDE";
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#discover(org.overlord.commons.dev.server.discovery.ModuleDiscoveryContext)
     */
    @Override
    public DevServerModule discover(ModuleDiscoveryContext context) {
        String path = someClass.getClassLoader()
                .getResource(someClass.getName().replace('.', '/') + ".class").getPath();
        if (path == null) {
            return null;
        }
        File file = new File(path);

        // The class file is available on the file system!  Just what we'd hoped.
        if (file.exists()) {
            if (path.contains("/WEB-INF/classes/")) {
                String pathToWebApp = path.substring(0, path.indexOf("/WEB-INF/classes/"));

                DevServerModule module = new DevServerModule();
                module.setInIDE(true);
                module.setModuleDir(new File(pathToWebApp));
                return module;
            }
        }

        return null;
    }

}
