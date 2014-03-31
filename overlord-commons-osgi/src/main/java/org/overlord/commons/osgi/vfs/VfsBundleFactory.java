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

package org.overlord.commons.osgi.vfs;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Implementation of the vfs bundle factory interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class VfsBundleFactory implements IVfsBundleFactory {
    
    private final Map<Long, VfsBundle> vfsBundleCache = new HashMap<Long, VfsBundle>();
    
    /**
     * Constructor.
     */
    public VfsBundleFactory() {
    }

    /**
     * @see org.overlord.commons.osgi.vfs.IVfsBundleFactory#getVfsBundle(java.net.URL)
     */
    @Override
    public synchronized VfsBundle getVfsBundle(URL url) {
        String host = url.getHost();
        long bundleId = Long.valueOf(host.split("\\.")[0]); //$NON-NLS-1$
        Bundle[] bundles = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles();
        Bundle theBundle = null;
        for (Bundle bundle : bundles) {
            if (bundle.getBundleId() == bundleId) {
                theBundle = bundle;
                break;
            }
        }
        
        // Shouldn't happen, but throw a runtime exception if it does.
        if (theBundle == null) {
            throw new RuntimeException(Messages.getString("VfsBundleFactory.BundleNotFound") + url); //$NON-NLS-1$
        }
        
        VfsBundle vfsBundle = vfsBundleCache.get(bundleId);
        if (vfsBundle == null) {
            vfsBundle = new VfsBundle(theBundle);
            vfsBundleCache.put(bundleId, vfsBundle);
        }
        
        return vfsBundle;
    }

}
