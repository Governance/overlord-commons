package org.overlord.commons.ant.crypt;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.overlord.commons.codec.AesEncrypter;

public class AesEncrypterTask extends Task {

    private String plain;

    private String addproperty;



    public String getPlain() {
        return plain;
    }

    public void setPlain(String plain) {
        this.plain = plain;
    }

    /**
     * Gets the addproperty.
     *
     * @return the addproperty
     */
    public String getAddproperty() {
        return addproperty;
    }

    /**
     * Sets the addproperty.
     *
     * @param addproperty
     *            the new addproperty
     */
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
        String generatedPassword="";

        if (addproperty == null || addproperty.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe output property is required for this task."); //$NON-NLS-1$
        }

        if (plain == null || plain.equals("")) { //$NON-NLS-1$
            throw new BuildException("\tThe plain text to encrypt property is required for this task."); //$NON-NLS-1$
        }
        // Get complete hashed password in hex format
        generatedPassword = AesEncrypter.encrypt(plain);

        if (addproperty != null && !addproperty.equals("")) { //$NON-NLS-1$
            getProject().setProperty(addproperty, generatedPassword);
        }
    }


}
