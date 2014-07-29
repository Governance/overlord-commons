package org.overlord.commons.karaf.commands;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class GenerateSamlKeystoreUtilTest {

    @Test
    public void testGenerate() throws Exception {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
        util.generate("overlord-password", temp);
        Assert.assertTrue(temp.exists());
        Assert.assertTrue(temp.length() > 0);
    }
}
