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
import java.io.IOException;

import org.overlord.commons.dev.server.DevServerModule;

/**
 * Discovers the module when presumably running from maven. Basically this strategy tries to find the module
 * by looking for a class on the classpath and assuming that it's in a JAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class WebAppModuleFromMavenDiscoveryStrategy extends AbstractModuleDiscoveryStrategy {

    private final Class<?> someClass;

    /**
     * Constructor.
     *
     * @param someClass
     */
    public WebAppModuleFromMavenDiscoveryStrategy(Class<?> someClass) {
        this.someClass = someClass;
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#getName()
     */
    @Override
    public String getName() {
        return "Maven Artifact";
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
        // The class file is available on the file system, so not what we're looking for.
        if (file.exists()) {
            return null;
        }

        if (path.contains("-classes.jar") && path.startsWith("file:")) {
            String pathToWar = path.substring(5, path.indexOf("-classes.jar")) + ".war";
            debug("Path to WAR: " + pathToWar);
            File war = new File(pathToWar);
            if (war.isFile()) {
                File workDir = new File(context.getTargetDir(), war.getName());
                debug("Work Dir: " + workDir);
                DevServerModule module = new DevServerModule();
                module.setInIDE(false);
                module.setWorkDir(workDir);
                module.setModuleDir(workDir);
                module.clean();
                workDir.mkdirs();
                try {
                    org.overlord.commons.dev.server.util.ArchiveUtils.unpackToWorkDir(war, workDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return module;
            }
        }

        return null;
    }

}
