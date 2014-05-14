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

package org.overlord.commons.auth.jetty8;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.overlord.commons.auth.util.IRoleGenerator;
import org.overlord.commons.auth.util.SAMLAssertionFactory;

/**
 * OSGi activator.
 *
 * @author eric.wittmann@redhat.com
 */
public class Activator implements BundleActivator {

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(SAMLAssertionFactory.class.getName(), new Jetty8SAMLAssertionFactory(), null);
        context.registerService(IRoleGenerator.class.getName(), new Jetty8RoleGenerator(), null);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
