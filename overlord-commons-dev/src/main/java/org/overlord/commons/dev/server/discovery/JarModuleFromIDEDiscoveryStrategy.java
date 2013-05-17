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
 * Finds a JAR module in the IDE.
 * @author eric.wittmann@redhat.com
 */
public class JarModuleFromIDEDiscoveryStrategy extends AbstractModuleDiscoveryStrategy {

    private final Class<?> someClass;
    private final String pathInProject;

    /**
     * Constructor.
     * @param someClass
     * @param pathInProject
     */
    public JarModuleFromIDEDiscoveryStrategy(Class<?> someClass, String pathInProject) {
        this.someClass = someClass;
        this.pathInProject = pathInProject;
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#getName()
     */
    @Override
    public String getName() {
        return "IDE (jar project)";
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

        debug("Path: " + path);

        File file = new File(path);
        // The class file is available on the file system!
        if (file.exists()) {
            if (path.contains("/target/classes/")) {
                String pathToProj = path.substring(0, path.indexOf("/target/classes/"));
                debug("Path to project: " + pathToProj);
                File modulePath = new File(pathToProj, this.pathInProject);
                debug("Module Path: " + modulePath);
                if (!modulePath.exists()) {
                    return null;
                }
                DevServerModule module = new DevServerModule();
                module.setInIDE(true);
                module.setModuleDir(modulePath);
                return module;
            }
        }
        return null;
    }

}
