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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.overlord.commons.osgi.Messages;

/**
 * A representation of an OSGi bundle as a vfs file.
 *
 * @author eric.wittmann@redhat.com
 */
public class VfsBundle {

    private File vfsBundleDir;
    private File vfsBundleIndexFile;
    private Map<String, File> index = new HashMap<String, File>();

    /**
     * Constructor.
     * @param bundle
     */
    public VfsBundle(Bundle bundle) {
        vfsBundleDir = getOrCreateVfsBundleDir(bundle);
        vfsBundleIndexFile = new File(vfsBundleDir, "index.properties"); //$NON-NLS-1$
        if (vfsBundleIndexFile.isFile()) {
            readVfsBundle();
        } else {
            createVfsBundle(bundle);
        }
    }

    /**
     * Creates a VFS Bundle from the given bundle, storing files in the given
     * directory.
     * @param vfsBundleDir
     * @param bundle
     */
    private void createVfsBundle(Bundle bundle) {
        Enumeration<?> entries = bundle.findEntries("", "*.*", true); //$NON-NLS-1$ //$NON-NLS-2$
        while (entries.hasMoreElements()) {
            Object nextElement = entries.nextElement();
            try {
                URL entryURL = new URL(String.valueOf(nextElement));
                File entryFile = writeBundleEntry(entryURL);
                if (isJar(entryFile)) {
                    indexJar(entryFile);
                } else if (isManifest(entryFile)) {
                    indexBundle(entryFile);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        writeIndex();
    }

    /**
     * Writes an entry from the bundle to the vfs bundle directory.
     * @param entryURL
     */
    private File writeBundleEntry(URL entryURL) {
        String path = entryURL.getPath();
        if (path == null || path.trim().length() == 0 || path.endsWith("/")) { //$NON-NLS-1$
            return null;
        }
        File entryFile = new File(vfsBundleDir, "content/" + path); //$NON-NLS-1$
        entryFile.getParentFile().mkdirs();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = entryURL.openStream();
            os = new FileOutputStream(entryFile);
            IOUtils.copy(is, os);
            return entryFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * Returns true if the given {@link File} is a jar file.
     * @param entryFile
     */
    private boolean isJar(File entryFile) {
        if (entryFile == null)
            return false;
        return entryFile.getName().toLowerCase().endsWith(".jar"); //$NON-NLS-1$
    }

    /**
     * Indexes the JAR file by getting a SHA1 hash of its MANIFEST.MF file.
     * @param entryFile
     */
    private void indexJar(File entryFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(entryFile);
            ZipEntry zipEntry = zipFile.getEntry("META-INF/MANIFEST.MF"); //$NON-NLS-1$
            if (zipEntry != null) {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                String hash = DigestUtils.shaHex(inputStream);
                index.put(hash, entryFile);
            }
        } catch (Exception e) {
            // Do nothing - invalid JAR file?
        } finally {
            try { if (zipFile != null) zipFile.close(); } catch (IOException e) {}
        }
    }

    /**
     * Returns true if the file is the manifest.mf file.
     * @param entryFile
     */
    private boolean isManifest(File entryFile) {
        if (entryFile == null)
            return false;
        return entryFile.getName().equals("MANIFEST.MF"); //$NON-NLS-1$
    }

    /**
     * Indexes the bundle (adds the bundle itself to the index).
     * @param entryFile
     */
    private void indexBundle(File entryFile) {
        InputStream is = null;
        try {
            is = new FileInputStream(entryFile);
            String hash = DigestUtils.shaHex(is);
            index.put(hash, new File(vfsBundleDir, "content")); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Writes the index 
     */
    private void writeIndex() {
        Properties props = new Properties();
        for (Entry<String, File> entry : index.entrySet()) {
            String value = vfsBundleDir.toURI().relativize(entry.getValue().toURI()).toString();
            props.setProperty(entry.getKey(), value);
        }
        Writer writer = null;
        try {
            writer = new FileWriter(vfsBundleIndexFile);
            props.store(writer, Messages.getString("VfsBundle.GeneratedBundleIndex")); //$NON-NLS-1$
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Reads the VfsBundle from the vfs bundle directory.
     */
    private void readVfsBundle() {
        Properties props = new Properties();
        Reader reader = null;
        try {
            reader = new FileReader(vfsBundleIndexFile);
            props.load(reader);
            for (Entry<Object, Object> entry : props.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String val = String.valueOf(entry.getValue());
                index.put(key, new File(vfsBundleDir, val));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Finds the fuse/karaf temp directory and returns a "errai-vfs/<bundleId>" sub directory.
     * @param bundle
     */
    private File getOrCreateVfsBundleDir(Bundle bundle) {
        String karafHome = System.getProperty("karaf.home"); //$NON-NLS-1$
        if (karafHome == null) {
            throw new RuntimeException(Messages.getString("VfsBundle.SystemPropertyMissing")); //$NON-NLS-1$
        }
        File tmpDir = new File(karafHome, "data/tmp/errai-vfs/" + bundle.getBundleId()); //$NON-NLS-1$
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        return tmpDir;
    }

    /**
     * Converts a URL to a File.  If the URL is the root URL for the bundle, then
     * this method will return a {@link File} that points to directory on the file
     * system.  However, if the URL points to a JAR within the bundle, then this 
     * method is responsible for figuring out which JAR is being referenced and 
     * then returning a {@link File} pointing to that JAR.
     * 
     * The approach to figuring out what the URL points to is as follows:
     * 1) Get the SHA1 hash of the MANIFEST.MF file returned via the URL
     * 2) Look up the File previously registered to that hash value
     * 
     * @param url
     */
    public File asFile(URL url) {
        InputStream manifestStream = null;
        try {
            String manifestUrl = "bundle://" + url.getHost() + ":" + url.getPort() + "/META-INF/MANIFEST.MF"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            URL manifestURL = new URL(manifestUrl);
            manifestStream = manifestURL.openStream();
            String manifestHash = DigestUtils.shaHex(manifestStream);
            
            File jarFile = index.get(manifestHash);
            if (jarFile != null) {
                return jarFile;
            }
        } catch (Exception e) {
            // TODO log the error
        } finally {
            IOUtils.closeQuietly(manifestStream);
        }
        
        throwNotFoundError(url);
        return null;
    }

    /**
     * Throws a "not found" error while also logging information found in the manifest.  This
     * latter part will help diagnose which JAR was *supposed* to have been detected.
     * @param url
     */
    private void throwNotFoundError(URL url) {
        InputStream manifestStream = null;
        StringBuilder builder = new StringBuilder();
        try {
            String manifestPath = "META-INF/MANIFEST.MF"; //$NON-NLS-1$
            URL manifestURL = new URL(url.toExternalForm() + manifestPath);
            manifestStream = manifestURL.openStream();
            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            for (Entry<Object, Object> entry : attributes.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                builder.append(key).append(": ").append(value).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (Exception e) {
        } finally {
            IOUtils.closeQuietly(manifestStream);
        }
        // Include the full manifest in the exception - this is helpful to diagnose which
        // JAR gave us fits.  Hopefully this will not happen!
        throw new RuntimeException("Failed to create a Vfs.Dir for URL: " + url + "\n--Manifest--\n====================" + builder.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
