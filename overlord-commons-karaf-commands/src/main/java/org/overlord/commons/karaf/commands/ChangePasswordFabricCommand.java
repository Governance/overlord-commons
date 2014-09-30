package org.overlord.commons.karaf.commands;

import org.apache.felix.gogo.commands.Command;
import org.overlord.commons.karaf.commands.configure.AbstractConfigureFabricCommand;

@Command(scope = "overlord:fabric", name = "change-password")
public class ChangePasswordFabricCommand extends AbstractConfigureFabricCommand {

    @Override
    protected Object doExecute() throws Exception {
        super.setAllowedPasswordOverwrite(true);
        super.doExecute();
        return null;
    }
}
