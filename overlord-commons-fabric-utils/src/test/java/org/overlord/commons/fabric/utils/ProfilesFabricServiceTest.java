/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.commons.fabric.utils;

import java.io.IOException;
import java.net.URL;

import javax.management.MalformedObjectNameException;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Provides the test cases for the {@link ProfilesFabricService} class. By
 * default it is set as @ignore because it is required that the fabric instance
 * is started. This means karaf console started and fabric created.
 * 
 * @author David Virgil Naranjo
 */
@Ignore
public class ProfilesFabricServiceTest {

    /**
     * Test creation profile.
     *
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     * @throws ConfigurationException
     *             the configuration exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreationProfile() throws MalformedObjectNameException, ConfigurationException, IOException {
        URL uri = this.getClass().getClassLoader().getResource("commons.profile.zip");
        ProfilesFabricService service = new ProfilesFabricService("http://localhost:8181/jolokia", "admin", "123admin!");
        service.createProfile("overlord", "commons", null, uri.openStream());
    }
}