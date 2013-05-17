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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.overlord.commons.dev.server.DevServerModule;

/**
 * Finds a web app from GAV information. Must be on the current classpath. This strategy is used when the web
 * app doesn't have any actual classes in it. In those cases, the -classes.jar of the web app *still* needs to
 * be on the classpath, but we search for the jar itself rather than a class in the jar.
 *
 * @author eric.wittmann@redhat.com
 */
public class WebAppModuleFromIDEGAVStrategy extends AbstractModuleDiscoveryStrategy {

    @SuppressWarnings("unused")
    private final String groupId;
    private final String artifactId;
    private final boolean useIdeSourcePath;

    /**
     * Constructor.
     *
     * @param groupId
     * @param artifactId
     */
    public WebAppModuleFromIDEGAVStrategy(String groupId, String artifactId, boolean useIdeSourcePath) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.useIdeSourcePath = useIdeSourcePath;
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
        URLClassLoader urlCL = (URLClassLoader) getClass().getClassLoader();
        TreeSet<URL> sortedURLs = new TreeSet<URL>(new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                return o1.toExternalForm().compareTo(o2.toExternalForm());
            }
        });
        sortedURLs.addAll(Arrays.asList(urlCL.getURLs()));

        String moduleUrl = null;

        for (URL url : sortedURLs) {
            String urlstr = url.toExternalForm();
            if (urlstr.contains("/" + artifactId + "/target")) {
                moduleUrl = urlstr;
                break;
            }
        }

        if (moduleUrl == null)
            return null;

        debug("Module URL: " + moduleUrl);

        try {
            String pathToWebApp = null;
            String replacementPath = "/target/" + artifactId;
            if (useIdeSourcePath)
                replacementPath = "/src/main/webapp";

            if (moduleUrl.contains("/target/classes")) {
                pathToWebApp = moduleUrl.replace("/target/classes", replacementPath);
            } else if (moduleUrl.contains("/target/" + artifactId + "/WEB-INF/classes")) {
                pathToWebApp = moduleUrl.replace("/target/" + artifactId + "/WEB-INF/classes", replacementPath);
            }

            if (pathToWebApp != null) {
                debug("Path to web app: " + pathToWebApp);
                File webApp = new File(new URL(pathToWebApp).toURI());
                if (webApp.exists()) {
                    DevServerModule module = new DevServerModule();
                    module.setInIDE(true);
                    module.setModuleDir(webApp);
                    return module;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
