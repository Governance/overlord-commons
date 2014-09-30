package org.overlord.commons.config.fabric;

import io.fabric8.api.FabricService;

import org.apache.commons.lang.text.StrLookup;
import org.overlord.commons.services.ServiceRegistryUtil;

public class ContainerLookup extends StrLookup {

    private FabricService fabricService;

    /**
     * Lazy load the fabric service.
     */
    private FabricService getFabricService() {
        if (fabricService == null) {
            try {
                fabricService = ServiceRegistryUtil.getSingleService(FabricService.class);
            } catch (Throwable t) {
            }
        }
        return fabricService;
    }

    @Override
    public String lookup(String key) {
        if (this.getFabricService() != null) {
            if (key.equals("httpUrl")) {
                return this.getFabricService().getCurrentContainer().getHttpUrl();
            }
        }
        return "container:" + key;
    }

}
