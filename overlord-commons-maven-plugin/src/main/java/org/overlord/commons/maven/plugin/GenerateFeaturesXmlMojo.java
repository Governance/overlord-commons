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

package org.overlord.commons.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
import org.overlord.commons.maven.plugin.featuresxml.FeaturesXml;

/**
 * A mojo that can generate a karaf features.xml file.
 * 
 * @author eric.wittmann@redhat.com
 */
@Mojo(name = "generate-features-xml", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateFeaturesXmlMojo extends AbstractMojo {

    @Parameter(property = "generate-features-xml.outputFile", defaultValue = "${project.build.outputDirectory}/features.xml")
    private String outputFile;

    @Parameter(property = "generate-features-xml.attach", defaultValue = "false")
    private String attach;

    @Parameter(property = "generate-features-xml.features")
    private List<Feature> features;

    @Parameter(property = "generate-features-xml.repositories")
    private List<String> repositories;

    @Component
    private MavenProject project;

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Component
    protected RepositorySystem repositorySystem;
    
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Constructor.
     */
    public GenerateFeaturesXmlMojo() {
    }

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("-------------------------------------------------");
        getLog().info("Generating Karaf compatible features.xml file to:");
        getLog().info("   " + outputFile);
        getLog().info("-------------------------------------------------");

        try {
            FeaturesXml featuresXml = new FeaturesXml();
            generate(featuresXml);
            File file = new File(outputFile);
            file.getParentFile().mkdirs();
            featuresXml.writeTo(file);
            
            if ("true".equals(attach)) {
                attachToBuild(file);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Attaches the features.xml file to the build.
     * @param file the generated features.xml file
     */
    private void attachToBuild(File file) {
        projectHelper.attachArtifact(this.project, "xml", "features", file);
    }

    /**
     * @param featuresXml
     */
    private void generate(FeaturesXml featuresXml) throws Exception {
        // Add the repositories
        if (this.repositories != null) {
            for (String repo : repositories) {
                featuresXml.addRepository(repo);
            }
        }

        // Collect all dependencies (bundle candidates)
        ScopeArtifactFilter filter = new ScopeArtifactFilter(DefaultArtifact.SCOPE_RUNTIME);
        DependencyNode dependencyGraph = dependencyGraphBuilder.buildDependencyGraph(project, filter);
        CollectingDependencyNodeVisitor collectingVizzy = new CollectingDependencyNodeVisitor();
        dependencyGraph.accept(collectingVizzy);
        List<DependencyNode> nodes = collectingVizzy.getNodes();

        // Iterate all features
        for (Feature feature : features) {
            getLog().info("Generating feature '" + feature.getName() + "'");
            // Create the feature
            featuresXml.addFeature(feature.getName(), feature.getVersion(), feature.getComment());
            // Add any feature dependencies
            List<Feature> onFeatures = feature.getDependsOnFeatures();
            if (onFeatures != null && !onFeatures.isEmpty()) {
                for (Feature onFeature : onFeatures) {
                    getLog().info(
                            "   Depends on feature: " + onFeature.getName() + "/" + onFeature.getVersion());
                    featuresXml.addFeatureDependency(feature.getName(), feature.getVersion(),
                            onFeature.getName(), onFeature.getVersion());
                }
            }
            // Add any included or non-excluded bundles (from artifact
            // dependency graph)
            PatternIncludesArtifactFilter includesFilter = new PatternIncludesArtifactFilter(
                    feature.getIncludes());
            PatternExcludesArtifactFilter excludesFilter = new PatternExcludesArtifactFilter(
                    feature.getExcludes());
            for (DependencyNode dependencyNode : nodes) {
                if (isSelf(dependencyNode))
                    continue;
                Artifact artifact = dependencyNode.getArtifact();
                // If no includes, assume everything
                boolean includeBundle = feature.getIncludes() == null || feature.getIncludes().isEmpty();
                if (includeBundle) {
                    getLog().debug("   Artifact " + artifact + " matches default [all] filter (including).");
                }
                if (includesFilter.include(artifact)) {
                    getLog().debug("   Artifact " + artifact + " matched include filter (including).");
                    includeBundle = true;
                }
                // Excludes must be explicit.
                if (!excludesFilter.include(artifact)) {
                    getLog().debug("   Artifact " + artifact + " matched exclude filter (excluding).");
                    includeBundle = false;
                }

                if (includeBundle) {
                    featuresXml.addBundle(feature.getName(), feature.getVersion(),
                            formatArtifactAsBundle(artifact));
                }
            }
            
            // Add additional explicit bundles specified in the config
            List<String> bundles = feature.getBundles();
            if (bundles != null && !bundles.isEmpty()) {
                for (String bundle : bundles) {
                    getLog().debug("   Adding explicit bundle: " + bundle);
                    featuresXml.addBundle(feature.getName(), feature.getVersion(), bundle);
                }
            }
        }
    }

    /**
     * Returns true if this dependency is really just ourselves.
     * @param dependencyNode
     */
    private boolean isSelf(DependencyNode dependencyNode) {
        return project.getGroupId().equals(dependencyNode.getArtifact().getGroupId()) &&
                project.getArtifactId().equals(dependencyNode.getArtifact().getArtifactId());
    }

    /**
     * Format the given artifact as a bundle string with the appropriate syntax
     * used by the karaf features.xml file. For example:
     * 
     * mvn:commons-configuration/commons-configuration/1.6
     * 
     * @param artifact
     */
    private String formatArtifactAsBundle(Artifact artifact) throws Exception {
        StringBuilder builder = new StringBuilder();
        // If it's a bundle already, awesome.  If not, we need to wrap it
        // and include some useful meta-data.
        if (isBundle(artifact)) {
            // Example:  mvn:commons-configuration/commons-configuration/1.6
            builder.append("mvn:");
            builder.append(artifact.getGroupId());
            builder.append("/");
            builder.append(artifact.getArtifactId());
            builder.append("/");
            builder.append(artifact.getBaseVersion());
            if (!"jar".equalsIgnoreCase(artifact.getType())) {
                builder.append("/");
                builder.append(artifact.getType());
            }
        } else {
            // Example:  wrap:mvn:log4j/log4j/1.2.14$Bundle-SymbolicName=log4j.log4j&amp;Bundle-Version=1.2.14&amp;Bundle-Name=Log4j
            builder.append("wrap:mvn:");
            builder.append(artifact.getGroupId());
            builder.append("/");
            builder.append(artifact.getArtifactId());
            builder.append("/");
            builder.append(artifact.getBaseVersion());
            if (!"jar".equalsIgnoreCase(artifact.getType())) {
                builder.append("/");
                builder.append(artifact.getType());
            }
            
            MavenProject project = resolveProject(artifact);
            builder.append("$Bundle-SymbolicName=");
            builder.append(artifact.getGroupId());
            builder.append(".");
            builder.append(artifact.getArtifactId());
            builder.append("&Bundle-Version=");
            builder.append(sanitizeVersionForOsgi(artifact.getBaseVersion()));
            if (project.getName() != null && project.getName().trim().length() > 0) {
                builder.append("&Bundle-Name=");
                builder.append(project.getName());
            }
        }
        return builder.toString();
    }

    /**
     * OSGi doesn't allow non-numeric components in version strings. So for
     * example a common maven version is 2.0.0-SNAPSHOT. This needs to be
     * converted to 2.0.0 so that OSGi will parse it without an exception. I
     * don't have a great way to do this generically, so we'll just need to
     * update this method with additional fixes as we find problematic version
     * strings.
     * @param version
     */
    private Object sanitizeVersionForOsgi(String version) {
        // Remove -SNAPSHOT
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        // Fix things like 1.3.5a (becomes 1.3.5)
        String ver = version.replaceAll("([0-9])[a-zA-Z]+", "$1");
        if (!ver.contains(".")) {
            return ver;
        }
        // Handle the case where there are only 2 numberic and one non-numeric component
        // like 1.7.Alpha.  Converts to 1.7.0.Alpha
        String[] split = ver.split("\\.");
        if (split.length == 3) {
            if (isNumeric(split[0]) && isNumeric(split[1]) && isAlpha(split[2])) {
                return split[0] + "." + split[1] + ".0." + split[2];
            }
        }
        return ver;
    }

    /**
     * @param versionComponent
     */
    private boolean isAlpha(String versionComponent) {
        return versionComponent.length() > 0 && Character.isLetter(versionComponent.charAt(0));
    }

    /**
     * @param versionComponent
     */
    private boolean isNumeric(String versionComponent) {
        for (int i = 0; i < versionComponent.length(); i++) {
            if (!Character.isDigit(versionComponent.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Detect if this artifact is already an osgi bundle.  If it is, then we don't need
     * to wrap it.  The best way to figure this out is to crack open the JAR and take a look
     * at the manifest.
     * @param artifact
     * @throws Exception 
     */
    private boolean isBundle(Artifact artifact) throws Exception {
        // Resolve the artifact.
        ArtifactResolutionRequest request = new ArtifactResolutionRequest().setArtifact(artifact);
        ArtifactResolutionResult result = repositorySystem.resolve(request);
        // If not found, then assume it's a reactor dependency and therefore should be a bundle.
        if (result.getArtifacts().isEmpty()) {
            getLog().info("Artifact " + artifact.toString() + " not found in local repository, assuming reactor dependency.");
            return true;
        }
        artifact = result.getArtifacts().iterator().next();
        if (!artifact.getFile().isFile()) {
            throw new Exception("Resolved artifact is not a file: " + artifact.getFile().getAbsolutePath());
        }
        
        // Crack open the dependency JAR, read the manifest, check for osgi attributes.
        JarFile jf = null;
        try {
            jf = new JarFile(artifact.getFile());
            Manifest manifest = jf.getManifest();
            if (manifest == null) {
                getLog().info("Artifact " + artifact.toString() + " missing a manifest!  Assuming not a bundle.");
                return false;
            }
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
                String value = attributes.getValue("Bundle-SymbolicName");
                if (value != null && value.trim().length() > 0) {
                    return true;
                }
            }
        } finally {
            jf.close();
        }
        
        return false;
    }

    /**
     * Resolves the given artifact to a maven project.
     * @param artifact
     * @throws Exception
     */
    private MavenProject resolveProject(Artifact artifact) throws Exception {
        Artifact pomArtifact = repositorySystem.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "", "pom");
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(pomArtifact);
        ArtifactResolutionResult resolved = repositorySystem.resolve(request);
        pomArtifact = resolved.getArtifacts().iterator().next();
        InputStream contentStream = null;
        MavenProject project = null;
        try {
            contentStream = new FileInputStream(pomArtifact.getFile());
            Model model = new MavenXpp3Reader().read(contentStream);
            project = new MavenProject(model);
        } finally {
            contentStream.close();
        }
        return project;
    }
}
