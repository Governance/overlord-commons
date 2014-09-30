package org.overlord.commons.config.fabric;

import io.fabric8.api.FabricService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrLookup;
import org.overlord.commons.services.ServiceRegistryUtil;

public class ProfileLookup extends StrLookup {

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
                byte[] fileContent = this.getFabricService().getCurrentContainer().getOverlayProfile().getFileConfiguration(key);
                OutputStream os = null;
                try {
                    File f = File.createTempFile(key, "");
                    os = new FileOutputStream(f);
                    f.deleteOnExit();
                    IOUtils.write(fileContent, os);
                    return f.toURI().toURL().toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
        }
        return "profile:" + key;
    }

}
