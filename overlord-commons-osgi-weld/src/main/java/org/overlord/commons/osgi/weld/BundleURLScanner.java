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

package org.overlord.commons.osgi.weld;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.weld.environment.servlet.deployment.URLScanner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.overlord.commons.osgi.vfs.IVfsBundleFactory;
import org.overlord.commons.osgi.vfs.VfsBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link URLScanner} capable of running in an OSGi environment.
 *
 * @author eric.wittmann@redhat.com
 */
public class BundleURLScanner extends URLScanner {

    private static final Logger log = LoggerFactory.getLogger(BundleURLScanner.class);

    private static final int MAXIMUM_TRIES = 10;

    private static final int MILLISECONDS_WAIT = 500;
    /**
     * Constructor.
     * @param classLoader
     * @param context
     */
    public BundleURLScanner(ClassLoader classLoader, ServletContext context) {
        super(classLoader);
    }

    /**
     * @see org.jboss.weld.environment.servlet.deployment.URLScanner#handleURL(java.net.URL, java.util.Set, java.util.Set)
     */
    @Override
    protected void handleURL(URL url, Set<String> classes, Set<URL> urls) {
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference serviceReference = null;
        int tries = 0;
        do {
            try {
                Thread.sleep(MILLISECONDS_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            serviceReference = bundleContext.getServiceReference(IVfsBundleFactory.class.getName());
        } while (serviceReference == null && tries < MAXIMUM_TRIES);

        if (serviceReference == null)
            throw new RuntimeException("Failed to find OSGi service [IVfsBundleFactory]."); //$NON-NLS-1$
        IVfsBundleFactory factory = (IVfsBundleFactory) bundleContext.getService(serviceReference);
        VfsBundle vfsBundle = factory.getVfsBundle(url);
        File file = vfsBundle.asFile(url);
        Set<String> paths = new HashSet<String>();
        if (file.isDirectory()) {
            File webClasses = new File(file, "WEB-INF/classes"); //$NON-NLS-1$
            if (webClasses.exists()) {
                paths.add(webClasses.getAbsolutePath());
            } else {
                // Don't include root bundles.  Essentially this means that Weld should
                // only scan what's in the WAR, not OSGi bundles that the WAR may reference
                //paths.add(file.getAbsolutePath());
            }
        } else {
            paths.add(file.getAbsolutePath());
        }
        handle(paths, classes, urls);
    }

    /**
     * @see org.jboss.weld.environment.servlet.deployment.URLScanner#scanResources(java.lang.String[], java.util.Set, java.util.Set)
     */
    @Override
    public void scanResources(String[] resources, Set<String> classes, Set<URL> urls) {
        for (String resourceName : resources) {
            try {
                Enumeration<URL> urlEnum = getClassLoader().getResources(resourceName);
                while (urlEnum.hasMoreElements()) {
                    URL url = urlEnum.nextElement();
                    handleURL(url, classes, urls);
                }
            } catch (IOException ioe) {
                log.warn("could not read: " + resourceName, ioe); //$NON-NLS-1$
            }
        }
    }

}
