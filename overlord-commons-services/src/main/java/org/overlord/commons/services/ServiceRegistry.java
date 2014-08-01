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

import java.util.Set;


/**
 * All service registry implementations must implement this interface.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ServiceRegistry {

    /**
     * Gets a single service.  If multiple services are registered for the service
     * interface, this method will fail.
     * @param service
     */
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException;

    /**
     * Gets a set of all the services that have been registered for the given service
     * interface.
     * @param serviceInterface
     */
    public <T> Set<T> getServices(Class<T> serviceInterface);

    /**
     * This method registers a service listener associated with the supplied service interface.
     * 
     * @param serviceInterface The service type
     * @param listener The service listener
     */
    public <T> void addServiceListener(Class<T> serviceInterface, ServiceListener<T> listener);

    /**
     * This method unregisters a service listener.
     * 
     * @param listener The service listener
     */
    public <T> void removeServiceListener(ServiceListener<T> listener);

}
