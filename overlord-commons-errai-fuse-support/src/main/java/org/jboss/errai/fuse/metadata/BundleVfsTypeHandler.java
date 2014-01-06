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

package org.jboss.errai.fuse.metadata;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.errai.reflections.vfs.SystemDir;
import org.jboss.errai.reflections.vfs.Vfs;
import org.jboss.errai.reflections.vfs.ZipDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.overlord.commons.osgi.vfs.IVfsBundleFactory;
import org.overlord.commons.osgi.vfs.VfsBundle;

/**
 * An {@link Vfs.UrlType} for deployment archives served through OSGi. <br/>
 * It will stream the bundle to a temporary location on disk and then 
 * delegate to {@link ZipDir}.
 * 
 * An example URL in Fuse 6.1 might be:  bundle://375.0:24/
 *
 * @author eric.wittmann@redhat.com
 */
public class BundleVfsTypeHandler implements Vfs.UrlType {

    private final static String BUNDLE = "bundle";

    /**
     * Constructor.
     */
    public BundleVfsTypeHandler() {
    }

    /**
     * @see org.jboss.errai.reflections.vfs.Vfs.UrlType#matches(java.net.URL)
     */
    @Override
    public boolean matches(URL url) {
        return url.getProtocol().equals(BUNDLE);
    }

    /**
     * @see org.jboss.errai.reflections.vfs.Vfs.UrlType#createDir(java.net.URL)
     */
    @Override
    public Vfs.Dir createDir(URL url) {
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference serviceReference = bundleContext.getServiceReference(IVfsBundleFactory.class.getName());
        if (serviceReference == null)
            throw new RuntimeException("OSGi Service Reference for [IVfsBundleFactory] not found.");
        IVfsBundleFactory vfsBundleFactory = (IVfsBundleFactory) bundleContext.getService(serviceReference);
        VfsBundle bundle = vfsBundleFactory.getVfsBundle(url);
        File vfsFile = bundle.asFile(url);
        try {
            if (vfsFile.isFile()) {
                return new ZipDir(vfsFile.toURI().toURL());
            }
            if (vfsFile.isDirectory()) {
                return new SystemDir(vfsFile.toURI().toURL());
            }
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
