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
package org.overlord.commons.maven.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * @author Brett Meyer
 *
 */
public class GenerateFeaturesXmlMojoTest {
    
    /**
     * SRAMP-523: GenerateFeaturesXmlMojo should prevent illegal chars in Bundle-Name
     * 
     * @throws Exception 
     */
    @Test
    public void testFormatArtifactAsBundle() throws Exception {
        GenerateFeaturesXmlMojo mojo = spy(new GenerateFeaturesXmlMojo());
        
        // isBundle == true
        doReturn(true).when(mojo).isBundle(any(Artifact.class));
        Artifact artifact = new DefaultArtifact("org.overlord.commons", "test-artifact", "0.0.1", "", "jar", "", null);
        mojo.formatArtifactAsBundle(artifact);
        String bundle = mojo.formatArtifactAsBundle(artifact);
        assertEquals("mvn:org.overlord.commons/test-artifact/0.0.1", bundle);
        
        // isBundle == false (normal project name)
        doReturn(false).when(mojo).isBundle(any(Artifact.class));
        MavenProject project = mock(MavenProject.class);
        doReturn(project).when(mojo).resolveProject(any(Artifact.class));
        when(project.getName()).thenReturn("Test Artifact");
        artifact = new DefaultArtifact("org.overlord.commons", "test-artifact", "0.0.1", "", "jar", "", null);
        mojo.formatArtifactAsBundle(artifact);
        bundle = mojo.formatArtifactAsBundle(artifact);
        assertEquals("wrap:mvn:org.overlord.commons/test-artifact/0.0.1$Bundle-SymbolicName=org.overlord.commons.test-artifact&Bundle-Version=0.0.1&Bundle-Name=Test Artifact", bundle);
        
        // isBundle == false (project name with unresolved variables)
        doReturn(false).when(mojo).isBundle(any(Artifact.class));
        doReturn(project).when(mojo).resolveProject(any(Artifact.class));
        when(project.getName()).thenReturn("${extension.name} API v.${spec.version}");
        artifact = new DefaultArtifact("org.overlord.commons", "test-artifact", "0.0.1", "", "jar", "", null);
        mojo.formatArtifactAsBundle(artifact);
        bundle = mojo.formatArtifactAsBundle(artifact);
        // Bundle-Name should be skipped
        assertEquals("wrap:mvn:org.overlord.commons/test-artifact/0.0.1$Bundle-SymbolicName=org.overlord.commons.test-artifact&Bundle-Version=0.0.1", bundle);
    }
}
