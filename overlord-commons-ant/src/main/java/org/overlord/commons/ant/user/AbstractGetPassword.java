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
package org.overlord.commons.ant.user;

import java.io.Console;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * Custom ant task that validates the user password during the installation of
 * overlord-commons in the different application servers
 *
 * @author David Virgil Naranjo
 *
 */
public abstract class AbstractGetPassword extends Task {


    private String addproperty;

    private String message;

    private String confirmationMessage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }



    public String getAddproperty() {
        return addproperty;
    }

    public void setAddproperty(String addproperty) {
        this.addproperty = addproperty;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        Console console = System.console();

        if (console == null) {
            throw new BuildException("\tConsole is not available");
        }
        if (addproperty == null || addproperty.equals("")) {
            throw new BuildException("\tThe output property is required for this task.");
        }

        if (message == null || message.equals("")) {
            throw new BuildException("\tThe message property is required for this task.");
        }

        if (confirmationMessage == null || confirmationMessage.equals("")) {
            throw new BuildException("\tThe confirmationMessage property is required for this task.");
        }

        String password = "";
        String repeatedPassword = "";
        boolean validated = false;

        do {
            console.printf(message);
            char[] readed = console.readPassword();
            password = new String(readed);
            console.printf(confirmationMessage);
            readed = console.readPassword();
            repeatedPassword = new String(readed);
            validated = validatePassword(password, repeatedPassword);

            if (validated) {
                try {
                    validated = validate(password);
                } catch (Exception re) {
                    validated = false;
                }
            }

        } while (!validated);

        if (addproperty != null && !addproperty.equals("")) {
            getProject().setNewProperty(addproperty, password);
        }

    }

    /**
     * Validate the password introduced by the user.
     *
     * @param password
     *            the password
     * @param repeatedPassword
     *            the repeated password
     * @return true, if successful
     */
    private boolean validatePassword(String password, String repeatedPassword) {
        if (password == null || password.trim().equals("")) {
            log("");
            log(" * Error *\nThe password should not be empty");
            return false;
        }
        if (repeatedPassword == null || repeatedPassword.trim().equals("")) {
            log("");
            log(" * Error *\nThe repeated password should not be empty");
            return false;
        }

        if (!password.equals(repeatedPassword)) {
            log("");
            log(" * Error *\nThe passwords you introduced do not match each other. Please write them again.");
            return false;
        }

        if (!password.matches(".*\\d+.*")) {
            log("");
            log(" * Error *\nThe passwords should include numbers.");
            return false;
        }
        if (password.length() < 8) {
            log("");
            log(" * Error *\nThe length of the password should be at least of 8 characters.");
            return false;
        }
        return true;
    }

    /**
     * Validate.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    protected abstract boolean validate(String password) throws Exception;





}
