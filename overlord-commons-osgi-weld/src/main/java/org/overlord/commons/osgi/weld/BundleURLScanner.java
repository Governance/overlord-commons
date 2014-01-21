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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.weld.environment.servlet.deployment.URLScanner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.overlord.commons.osgi.vfs.IVfsBundleFactory;
import org.overlord.commons.osgi.vfs.VfsBundle;

/**
 * A {@link URLScanner} capable of running in an OSGi environment.
 *
 * @author eric.wittmann@redhat.com
 */
public class BundleURLScanner extends URLScanner {

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
        ServiceReference serviceReference = bundleContext.getServiceReference(IVfsBundleFactory.class.getName());
        if (serviceReference == null)
            throw new RuntimeException("Failed to find OSGi service [IVfsBundleFactory].");
        IVfsBundleFactory factory = (IVfsBundleFactory) bundleContext.getService(serviceReference);
        VfsBundle vfsBundle = factory.getVfsBundle(url);
        File file = vfsBundle.asFile(url);
        Set<String> paths = new HashSet<String>();
        if (file.isDirectory()) {
            File webClasses = new File(file, "WEB-INF/classes");
            if (webClasses.exists()) {
                paths.add(webClasses.getAbsolutePath());
            } else {
                paths.add(file.getAbsolutePath());
            }
        }
        handle(paths, classes, urls);
    }

}
