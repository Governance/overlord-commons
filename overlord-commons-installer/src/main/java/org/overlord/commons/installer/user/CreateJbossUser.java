package org.overlord.commons.installer.user;

import java.io.Console;
import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CreateJbossUser extends Task {

    private String jbossHome;


    @Override
    public void execute() throws BuildException {
        Console console = System.console();

        if (console == null) {
            throw new BuildException("\tConsole is not available");
        }

        if (!(jbossHome != null && !jbossHome.equals(""))) {
            throw new BuildException(
                    "The jbossHome property has to be set to follow the creation of the user");
        }

        String password = "";
        String repeatedPassword = "";
        boolean validated = false;
        String argsJboss = "";
        String argArray[];
        String appServerModulePath = "";
        if (jbossHome.endsWith(File.pathSeparator)) {
            appServerModulePath = jbossHome + "modules";
        } else {
            appServerModulePath = jbossHome + File.pathSeparator + "modules";
        }

            do {
                console.printf("\tPlease enter a password for the Overlord 'admin' user: ");
                char[] readed = console.readPassword();
                password = new String(readed);
                console.printf("\tPlease write again the password: ");
                readed = console.readPassword();
                repeatedPassword = new String(readed);
                validated = validatePassword(password, repeatedPassword);

                if (validated) {
                    argsJboss = "-mp " + appServerModulePath
                            + " org.jboss.as.domain-add-user -a -s -u admin -p " + password
                        + " -r ApplicationRealm -ro overlorduser,admin.sramp,dev,qa,stage,prod,ba,arch "
                        + "--jboss_home " + jbossHome;
                    argArray = argsJboss.split(" ");
                    try {
                    org.overlord.commons.installer.user.AddPropertiesUser.main(argArray);
                    } catch (RuntimeException re) {
                        validated = false;
                    }
                }

            } while (!validated);


    }


    public String getJbossHome() {
        return jbossHome;
    }

    public void setJbossHome(String jbossHome) {
        this.jbossHome = jbossHome;
    }

    private boolean validatePassword(String password, String repeatedPassword) {
        if (password == null || password.trim().equals("")) {
            System.out.println("\n * Error *\nThe password should not be empty");
            return false;
        }
        if (repeatedPassword == null || repeatedPassword.trim().equals("")) {
            System.out.println("\n * Error *\nThe repeated password should not be empty");
            return false;
        }

        if (!password.equals(repeatedPassword)) {
            System.out
                    .println("\n * Error *\nThe passwords you introduced do not match each other. Please write them again.");
            return false;
        }

        return true;
    }




}
