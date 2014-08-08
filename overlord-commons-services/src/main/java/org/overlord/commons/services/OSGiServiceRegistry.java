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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

/**
 * Implements a service registry by delegating to the osgi service
 * registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class OSGiServiceRegistry extends AbstractServiceRegistry {
    
    private static final Logger LOG=Logger.getLogger(OSGiServiceRegistry.class.getName());

    private java.util.Map<ServiceListener<?>, ServiceListenerAdapter<?>> _listeners=
                            new java.util.HashMap<ServiceListener<?>, ServiceListenerAdapter<?>>();
    
    private java.util.List<Object> _services=new java.util.ArrayList<Object>();

    /**
     * Constructor.
     */
    public OSGiServiceRegistry() {
    }

    @Override
    protected void init(Object service) {
        synchronized(_services) {
            if (!_services.contains(service)) {
                super.init(service);
            }
            _services.add(service);
        }
    }
    
    @Override
    protected void close(Object service) {
        synchronized(_services) {
            // Check if service is removed from the list, and that no other instances
            // of that service remain, before calling close. This allows multiple references
            // to the same service to be used and only closing it once the last reference
            // has been removed.
            if (_services.remove(service)
                    && !_services.contains(service)) {
                super.close(service);
            }
        }
    }
    
    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        // TODO use the osgi service tracker here
        T service = null;
        try {
            Bundle bundle = FrameworkUtil.getBundle(serviceInterface);
            if (bundle != null) {
                BundleContext context = bundle.getBundleContext();
                
                if (context != null) {
                    ServiceReference[] serviceReferences = context.getServiceReferences(serviceInterface.getName(), null);
                    if (serviceReferences != null) {
                        if (serviceReferences.length == 1) {
                            service = (T) context.getService(serviceReferences[0]);
                            init(service);
                        } else {
                            throw new IllegalStateException(Messages.getString("OSGiServiceRegistry.MultipleImplsRegistered") + serviceInterface); //$NON-NLS-1$
                        }
                    }
                } else {
                    LOG.warning(Messages.format("OSGiServiceRegistry.MissingBundleContext", serviceInterface)); //$NON-NLS-1$
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getServices(Class<T> serviceInterface) {
        Set<T> services = new HashSet<T>();
        try {
            Bundle bundle = FrameworkUtil.getBundle(serviceInterface);
            if (bundle != null) {
                if (bundle.getState() == Bundle.RESOLVED) {
                    bundle.start();
                }
                BundleContext context = bundle.getBundleContext();
                if (context != null) {
                    ServiceReference[] serviceReferences = context.getServiceReferences(
                            serviceInterface.getName(), null);
                    if (serviceReferences != null) {
                        for (ServiceReference serviceReference : serviceReferences) {
                            T service = (T) context.getService(serviceReference);
                            init(service);
                            services.add(service);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    public <T> void addServiceListener(Class<T> serviceInterface, ServiceListener<T> listener) {
        synchronized (_listeners) {
            _listeners.put(listener, new ServiceListenerAdapter<T>(serviceInterface, listener, this));
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> void removeServiceListener(ServiceListener<T> listener) {
        synchronized (_listeners) {
            @SuppressWarnings("unchecked")
            ServiceListenerAdapter<T> adapter=(ServiceListenerAdapter<T>)_listeners.get(listener);
            
            if (adapter != null) {
                adapter.close();
                _listeners.remove(listener);
            }
        }
    }

    /**
     * This class bridges between the OSGi service listener and the commons service listener.
     * 
     */
    public static class ServiceListenerAdapter<T> {
        
        private Class<T> _serviceInterface;
        private ServiceListener<T> _serviceListener;
        private OSGiServiceRegistry _serviceRegistry;
        
        private org.osgi.framework.ServiceListener _osgiListener=null;
        
        public ServiceListenerAdapter(Class<T> serviceInterface, ServiceListener<T> listener, OSGiServiceRegistry reg) {
            _serviceInterface = serviceInterface;
            _serviceListener = listener;
            _serviceRegistry = reg;
            
            init();
        }
        
        @SuppressWarnings("unchecked")
        protected void init() {
            Bundle bundle = FrameworkUtil.getBundle(_serviceInterface);
            if (bundle != null) {
                final BundleContext context = bundle.getBundleContext();
                
                _osgiListener = new org.osgi.framework.ServiceListener() {
                    public void serviceChanged(ServiceEvent ev) {
                        ServiceReference sr = ev.getServiceReference();
                        T service=(T)context.getService(sr);
                        switch(ev.getType()) {
                        case ServiceEvent.REGISTERED:
                            _serviceRegistry.init(service);
                            _serviceListener.registered(service);
                            break;
                        case ServiceEvent.UNREGISTERING:
                            _serviceListener.unregistered(service);
                            _serviceRegistry.close(service);
                            break;
                        default:
                            break;
                        }
                    }           
                };
                
                String filter = "(objectclass=" + _serviceInterface.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    context.addServiceListener(_osgiListener, filter);
                } catch (InvalidSyntaxException e) { 
                    LOG.log(Level.SEVERE, Messages.format("OSGiServiceRegistry.FailedToAddListener", _serviceInterface.getName())); //$NON-NLS-1$
                }

                ServiceReference[] srefs;
                try {
                    srefs = context.getServiceReferences(_serviceInterface.getName(), null);
                    
                    if (srefs != null) {
                        for (int i=0; i < srefs.length; i++) {
                            T service=(T)context.getService(srefs[i]);
                            _serviceRegistry.init(service);
                            _serviceListener.registered(service);
                        }
                    }
                } catch (InvalidSyntaxException e) {
                    LOG.log(Level.SEVERE, Messages.format("OSGiServiceRegistry.FailedToAddServiceReferences", _serviceInterface.getName())); //$NON-NLS-1$
                }
            }
        }
        
        public void close() {
            
        }
    }
}

