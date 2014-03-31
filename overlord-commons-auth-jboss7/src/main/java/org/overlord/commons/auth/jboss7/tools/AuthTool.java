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
package org.overlord.commons.auth.jboss7.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.as.security.vault.VaultSession;

/**
 * A handy authentication tool for Overlord projects.  This tool can do two things.
 *
 * 1) Create an overlord user
 * 2) Store a password in the password vault
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthTool {

    /**
     * @param args
     */
    public static void main(String [] args) {
        try {
            Options options = Options.parse(args);
            if (options.execType == ExecType.storepassword) {
                storePassword(options);
            }
            if (options.execType == ExecType.adduser) {
                addUser(options);
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Stores a password in the password vault.
     * <pre>
     *  Command line options for this command:
     *   -vaultdir
     *   -keystore
     *   -storepass
     *   -alias
     *   -salt
     *   -count
     *   -name
     *   -password
     *   -block
     *   -propertyfile [optional]
     *   -property [optional]
     * </pre>
     * @param options
     * @throws Exception
     */
    protected static void storePassword(Options options) throws Exception {
        System.out.println("Storing a password in the password vault.");
        String vaultdir = options.cmdLineOptions.get("vaultdir"); //$NON-NLS-1$
        String keystore = options.cmdLineOptions.get("keystore"); //$NON-NLS-1$
        String storepass = options.cmdLineOptions.get("storepass"); //$NON-NLS-1$
        String alias = options.cmdLineOptions.get("alias"); //$NON-NLS-1$
        String salt = options.cmdLineOptions.get("salt"); //$NON-NLS-1$
        String count = options.cmdLineOptions.get("count"); //$NON-NLS-1$
        String block = options.cmdLineOptions.get("block"); //$NON-NLS-1$
        String name = options.cmdLineOptions.get("name"); //$NON-NLS-1$
        String password = options.cmdLineOptions.get("password"); //$NON-NLS-1$
        String propertyfile = options.cmdLineOptions.get("propertyfile"); //$NON-NLS-1$
        String property = options.cmdLineOptions.get("property"); //$NON-NLS-1$

        if (vaultdir == null || keystore == null || storepass == null || salt == null
                || alias == null || count == null || name == null || password == null
                || block == null) {
            throw new Exception("Missing required argument.");
        }

        VaultSession session = new VaultSession(keystore, storepass, vaultdir, salt, Integer.parseInt(count));
        session.startVaultSession(alias);
        String vaultHash = session.addSecuredAttribute(block, name, password.toCharArray());

        System.out.println("Password stored in vault.  Vault hash is:");
        System.out.println(vaultHash);

        if (property != null && propertyfile != null) {
            outputResult(vaultHash, propertyfile, property);
        }
    }

    /**
     * Stores a password in the password vault.
     * <pre>
     *  Command line options for this command:
     *   -configdir
     *   -vaultdir
     *   -keystore
     *   -storepass
     *   -alias
     *   -salt
     *   -count
     *   -user
     *   -password
     *   -roles  (comma separated list of roles)
     *   -propertyfile [optional]
     *   -property [optional]
     * </pre>
     * @param options
     * @throws Exception
     */
    protected static void addUser(Options options) throws Exception {
        System.out.println("Adding an Overlord user.");

        String configdir = options.cmdLineOptions.get("configdir"); //$NON-NLS-1$
        String vaultdir = options.cmdLineOptions.get("vaultdir"); //$NON-NLS-1$
        String keystore = options.cmdLineOptions.get("keystore"); //$NON-NLS-1$
        String storepass = options.cmdLineOptions.get("storepass"); //$NON-NLS-1$
        String alias = options.cmdLineOptions.get("alias"); //$NON-NLS-1$
        String salt = options.cmdLineOptions.get("salt"); //$NON-NLS-1$
        String count = options.cmdLineOptions.get("count"); //$NON-NLS-1$
        String user = options.cmdLineOptions.get("user"); //$NON-NLS-1$
        String password = options.cmdLineOptions.get("password"); //$NON-NLS-1$
        String roles = options.cmdLineOptions.get("roles"); //$NON-NLS-1$
        String propertyfile = options.cmdLineOptions.get("propertyfile"); //$NON-NLS-1$
        String property = options.cmdLineOptions.get("property"); //$NON-NLS-1$

        String block = "overlord"; //$NON-NLS-1$
        String name = user + ".password"; //$NON-NLS-1$

        if (vaultdir == null || keystore == null || storepass == null || salt == null
                || alias == null || count == null || user == null || password == null) {
            throw new Exception("Missing required argument.");
        }

        VaultSession session = new VaultSession(keystore, storepass, vaultdir, salt, Integer.parseInt(count));
        session.startVaultSession(alias);
        String vaultHash = session.addSecuredAttribute(block, name, password.toCharArray());

        System.out.println("New Overlord user's password stored in vault.  Vault hash is:");
        System.out.println(vaultHash);

        createUser(user, roles, vaultHash, configdir);

        if (property != null && propertyfile != null) {
            outputResult(vaultHash, propertyfile, property);
        }

        System.out.println("Overlord user succesfully created.");
    }

    /**
     * Creates a user in the Overlord config files.
     * @param user
     * @param roles
     * @param vaultHash
     * @param configdir
     */
    private static void createUser(String user, String roles, String vaultHash, String configdir) throws Exception {
        File usersFile = new File(configdir, "overlord-idp-users.properties"); //$NON-NLS-1$
        File rolesFile = new File(configdir, "overlord-idp-roles.properties"); //$NON-NLS-1$

        FileWriter writer = null;
        try {
            writer = new FileWriter(usersFile, true);
            writer.write(user);
            writer.write("="); //$NON-NLS-1$
            writer.write(vaultHash);
            writer.write("\n"); //$NON-NLS-1$
            writer.flush();
        } finally {
            writer.close();
        }

        try {
            writer = new FileWriter(rolesFile, true);
            writer.write(user);
            writer.write("="); //$NON-NLS-1$
            if (roles == null) {
                writer.write("overlorduser,admin.sramp"); //$NON-NLS-1$
            } else {
                writer.write("overlorduser,admin.sramp," + roles); //$NON-NLS-1$
            }
            writer.write("\n"); //$NON-NLS-1$
            writer.flush();
        } finally {
            writer.close();
        }
    }

    /**
     * Stores the resulting vault password hash to a properties file.
     * @param vaultHash
     * @param propertyfile
     * @param property
     */
    private static void outputResult(String vaultHash, String propertyfile, String property) throws Exception {
        Properties props = new Properties();
        props.put(property, vaultHash);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(propertyfile);
            props.store(fos, property);
        } finally {
            try { fos.close(); } catch (Exception e) { }
        }

    }

    private static final class Options {
        public ExecType execType;
        public Map<String, String> cmdLineOptions = new HashMap<String, String>();

        public static final Options parse(String [] args) throws Exception {
            if (args.length == 0) {
                throw new Exception("Missing first argument (tool type).");
            }
            Options options = new Options();
            String et = args[0];
            options.execType = ExecType.valueOf(et);
            for (int i = 1; i < args.length; i++) {
                if (args[i].startsWith("-")) { //$NON-NLS-1$
                    String key = args[i].substring(1);
                    String value = null;
                    if ( (i+1) < args.length && !args[i+1].startsWith("-")) { //$NON-NLS-1$
                        value = args[i+1];
                    }
                    options.cmdLineOptions.put(key, value);
                }
            }
            return options;
        }
    }

    private static enum ExecType {
        adduser, storepassword;
    }

}
