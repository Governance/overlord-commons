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

/**
 * A classloader that, when asked to return a list of beans.xml URLs, will filter the resulting
 * list based on some criteria.
 *
 * @author eric.wittmann@redhat.com
 */
public class FilteringClassLoader extends ClassLoader {
    
    private ClassLoader delegate;
    
    /**
     * Constructor.
     * @param delegate
     */
    public FilteringClassLoader(ClassLoader delegate) {
        this.delegate = delegate;
    }

}
