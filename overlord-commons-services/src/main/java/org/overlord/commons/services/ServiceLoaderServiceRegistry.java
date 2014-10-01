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

package org.overlord.commons.services;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Implements a service registry by using the standard java {@link ServiceLoader}
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceLoaderServiceRegistry extends AbstractServiceRegistry {
    
    private Map<Class<?>, Set<?>> servicesCache = new HashMap<Class<?>, Set<?>>();

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        // Cached single service values are derived from the values cached when checking
        // for multiple services
        T rval = null;
        Set<T> services=getServices(serviceInterface);
        
        if (services.size() > 1) {
            throw new IllegalStateException(Messages.getString("ServiceLoaderServiceRegistry.MultipleImplsFound") + serviceInterface); //$NON-NLS-1$
        } else if (!services.isEmpty()) {
            rval = services.iterator().next();
        }
        return rval;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getServices(Class<T> serviceInterface) {
        synchronized(servicesCache) {
            if (servicesCache.containsKey(serviceInterface))
                return (Set<T>) servicesCache.get(serviceInterface);
    
            Set<T> services = new LinkedHashSet<T>();
            try {
                for (T service : ServiceLoader.load(serviceInterface)) {
                    init(service);
                    services.add(service);
                }
            } catch (ServiceConfigurationError sce) {
                // No services found - don't check again.
            }
            servicesCache.put(serviceInterface, services);
            return services;
        }
    }

}
