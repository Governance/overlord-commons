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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.overlord.commons.dev.server.DevServerModule;

/**
 * Finds a web app from GAV information.  Must be on the current classpath.  This
 * strategy is used when the web app doesn't have any actual classes in it.  In
 * those cases, the -classes.jar of the web app *still* needs to be on the classpath,
 * but we search for the jar itself rather than a class in the jar.
 * @author eric.wittmann@redhat.com
 */
public class WebAppModuleFromMavenGAVStrategy extends AbstractModuleDiscoveryStrategy {

    private final String groupId;
    private final String artifactId;

    /**
     * Constructor.
     * @param groupId
     * @param artifactId
     */
    public WebAppModuleFromMavenGAVStrategy(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#getName()
     */
    @Override
    public String getName() {
        return "Maven Artifact (GAV)";
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.IModuleDiscoveryStrategy#discover(org.overlord.commons.dev.server.discovery.ModuleDiscoveryContext)
     */
    @Override
    public DevServerModule discover(ModuleDiscoveryContext context) {
        URLClassLoader urlCL = (URLClassLoader) getClass().getClassLoader();
        TreeSet<URL> sortedURLs = new TreeSet<URL>(new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                return o1.toExternalForm().compareTo(o2.toExternalForm());
            }
        });
        sortedURLs.addAll(Arrays.asList(urlCL.getURLs()));

        String moduleUrl = null;

        // Look for something that looks like a maven path
        String groupIdAsPath = groupId.replace('.', '/');
        for (URL url : sortedURLs) {
            String urlstr = url.toExternalForm();
            if (urlstr.contains(groupIdAsPath) && urlstr.contains("/"+artifactId+"-")) {
                moduleUrl = urlstr;
                break;
            }
        }

        if (moduleUrl == null)
            return null;

        debug("Module URL: " + moduleUrl);

        try {
            String pathToWar = moduleUrl.replace("-classes.jar", ".war");
            URL warUrl = new URL(pathToWar);
            File war = new File(warUrl.toURI());
            debug("WAR: " + war);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
