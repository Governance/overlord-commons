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

package org.overlord.commons.auth.util;

import java.util.Set;

import org.overlord.commons.auth.filters.HttpRequestThreadLocalFilter;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * A utility for dealing with user roles.
 *
 * @author eric.wittmann@redhat.com
 */
public class RoleUtil {
	
	private static IRoleGenerator cachedGenerator;

    /**
     * Generates roles for the current user.
     */
    public static Set<String> generateRoles() {
    	IRoleGenerator generator = getRoleGenerator();
    	if (generator == null) {
            throw new RuntimeException("Failed to generate user roles:  Unsupported/undetected platform."); //$NON-NLS-1$
    	} else {
    		return generator.generateRoles(HttpRequestThreadLocalFilter.TL_request.get());
    	}
    }

	/**
	 * @return the role generator for the current runtime platform
	 */
	private static IRoleGenerator getRoleGenerator() {
		if (cachedGenerator != null) {
			return cachedGenerator;
		}
		
        Set<IRoleGenerator> generators = null;
        
        // Note: use our classloader when loading the services because the application-specific
        // overlord-commons-auth-* implementations will likely be packaged up with the generic
        // overlord-commons-auth (this) module.  The exception being OSGi, which 
        // doesn't use ServiceLoader anyway.
        //
        // For example, when running in JBoss EAP 6.x, all of the overlord-commons-auth* JARs
        // are bundled up into a single JBoss Module.  In order for the ServiceLoader to work
        // properly, the context classloader would need to be set to the module's CL (so that
        // the service files are visible).
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(RoleUtil.class.getClassLoader());
        try {
        	generators = ServiceRegistryUtil.getServices(IRoleGenerator.class);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
        
        // Now that the factories are loaded, go ahead and try to use one of them.
        for (IRoleGenerator generator : generators) {
            if (generator.accept()) {
            	cachedGenerator = generator;
            	return cachedGenerator;
            }
        }
        return null;
	}

}
