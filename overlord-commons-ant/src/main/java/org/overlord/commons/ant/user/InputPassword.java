package org.overlord.commons.ant.user;

import java.io.Console;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class InputPassword extends Task {
    private String addproperty;
    private String message;
    private String confirmationMessage;
    private boolean password;

    private Integer minimumLength;

    private boolean numbersRequired;

    private boolean nonAlphaNumericsRequired;

    public String getAddproperty() {
        return addproperty;
    }

    public void setAddproperty(String addproperty) {
        this.addproperty = addproperty;
    }

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

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public Integer getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(Integer minimumLength) {
        this.minimumLength = minimumLength;
    }

    public boolean isNumbersRequired() {
        return numbersRequired;
    }

    public void setNumbersRequired(boolean numbersRequired) {
        this.numbersRequired = numbersRequired;
    }

    public boolean isNonAlphaNumericsRequired() {
        return nonAlphaNumericsRequired;
    }

    public void setNonAlphaNumericsRequired(boolean nonAlphaNumericsRequired) {
        this.nonAlphaNumericsRequired = nonAlphaNumericsRequired;
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

        // Only prompt for the password if it hasn't already been set.
        String currentPropVal = getProject().getProperty(addproperty);
        if (currentPropVal == null || currentPropVal.trim().isEmpty()) {
            String value = ""; //$NON-NLS-1$
            String repeatedPassword = ""; //$NON-NLS-1$
            boolean validated = false;

            do {
                validated = true;
                // Read the password
                console.printf(message);

                if (isPassword()) {
                    char[] readed = null;
                    readed = console.readPassword();
                    value = new String(readed);
                } else {
                    value = console.readLine();
                }

                validated = validate(value);
                if (validated) {
                    // Now confirm the password
                    console.printf(confirmationMessage);

                    if (isPassword()) {
                        repeatedPassword = new String(console.readPassword());
                    } else {
                        repeatedPassword = console.readLine();
                    }

                    if (!value.equals(repeatedPassword)) {
                        log(""); //$NON-NLS-1$
                        log(" * Error *\nThe values you entered do not match. Please try again."); //$NON-NLS-1$
                        validated = false;
                    }
                }

            } while (!validated);

            if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
                getProject().setProperty(addproperty, value);
            }
        }

    }

    private boolean validate(String value) {
        if (minimumLength != null && minimumLength > 0) {
            if (value.length() < minimumLength) {
                log(" * Error *\nThe length of the password should be at least " + minimumLength + " characters."); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
        }
        if (numbersRequired && !value.matches(".*\\d+.*")) { //$NON-NLS-1$
            log(""); //$NON-NLS-1$
            log(" * Error *\nThe password should include at least one number."); //$NON-NLS-1$
            return false;
        }
        if (nonAlphaNumericsRequired && !value.matches("^.*[^a-zA-Z0-9 ].*$")) { //$NON-NLS-1$
            log(" * Error *\nThe password should contain at least one non-alphanumeric (symbol) character."); //$NON-NLS-1$
            return false;
        }

        return true;
    }
}
