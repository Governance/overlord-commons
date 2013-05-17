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
 * Finds a jar module on the maven classpath.
 *
 * @author eric.wittmann@redhat.com
 */
public class JarModuleFromMavenDiscoveryStrategy extends AbstractModuleDiscoveryStrategy {

    private final Class<?> someClass;
    private final String pathInJar;

    /**
     * Constructor.
     *
     * @param class1
     * @param string
     */
    public JarModuleFromMavenDiscoveryStrategy(Class<?> someClass, String pathInJar) {
        this.someClass = someClass;
        this.pathInJar = pathInJar;
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#getName()
     */
    @Override
    public String getName() {
        return "Maven JAR module";
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
        if (!file.exists() && path.contains(".jar") && path.startsWith("file:")) {
            String pathToJar = path.substring(5, path.indexOf(".jar")) + ".jar";
            debug("Path to JAR: " + pathToJar);
            File jar = new File(pathToJar);
            if (jar.isFile()) {
                File workDir = new File(context.getTargetDir(), jar.getName());
                debug("Work Dir: " + workDir);

                DevServerModule module = new DevServerModule();
                module.setInIDE(false);
                module.setWorkDir(workDir);
                module.clean();
                workDir.mkdirs();
                try {
                    org.overlord.commons.dev.server.util.ArchiveUtils.unpackToWorkDir(jar, workDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File moduleDir = new File(workDir, this.pathInJar);
                if (!moduleDir.exists()) {
                    throw new RuntimeException("JAR found (" + jar + "), but path-in-JAR not found: "
                            + this.pathInJar);
                }
                module.setModuleDir(moduleDir);
                return module;
            }
        }

        return null;
    }

}
