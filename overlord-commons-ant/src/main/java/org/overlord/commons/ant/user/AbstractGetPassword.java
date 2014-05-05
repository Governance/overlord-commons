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
            throw new BuildException("\tConsole is not available"); //$NON-NLS-1$
        }
        if (addproperty == null || addproperty.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe output property is required for this task."); //$NON-NLS-1$
        }

        if (message == null || message.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe message property is required for this task."); //$NON-NLS-1$
        }

        if (confirmationMessage == null || confirmationMessage.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe confirmationMessage property is required for this task."); //$NON-NLS-1$
        }

        String password = ""; //$NON-NLS-1$
        String repeatedPassword = ""; //$NON-NLS-1$
        boolean validated = false;

        do {
            // Read the password
            console.printf(message);
            char[] readed = console.readPassword();
            password = new String(readed);

            validated = validatePassword(password) && validate(password);

            if (validated) {
                // Now confirm the password
                console.printf(confirmationMessage);
                readed = console.readPassword();
                repeatedPassword = new String(readed);

                if (!password.equals(repeatedPassword)) {
                    log(""); //$NON-NLS-1$
                    log(" * Error *\nThe passwords you entered do not match. Please try again."); //$NON-NLS-1$
                    validated = false;
                }
            }
        } while (!validated);

        if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
            getProject().setNewProperty(addproperty, password);
        }

    }

    /**
     * Validate the password introduced by the user.
     *
     * @param password the password
     * @return true, if successful
     */
    private boolean validatePassword(String password) {
        if (password == null || password.trim().equals("")) { //$NON-NLS-1$
            log(""); //$NON-NLS-1$
            log(" * Error *\nThe password should not be empty"); //$NON-NLS-1$
            return false;
        }
        if (!password.matches(".*\\d+.*")) { //$NON-NLS-1$
            log(""); //$NON-NLS-1$
            log(" * Error *\nThe password should include at least one number."); //$NON-NLS-1$
            return false;
        }
        if (password.length() < 8) {
            log(""); //$NON-NLS-1$
            log(" * Error *\nThe length of the password should be at least 8 characters."); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * Validate.
     * @return true, if the password is valid
     */
    protected abstract boolean validate(String password);

}
