package com.starxg.keytar;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * KeytarTest
 * 
 * @author huangxingguang@lvmama.com
 */
public class KeytarTest {
    private static final String SERVICE_NAME = "com.starxg.keytar";

    @Test
    public void test() throws KeytarException {
        Keytar instance = Keytar.getInstance();

        // set
        instance.setPassword(SERVICE_NAME, "tom", "123456");
        Assert.assertEquals(instance.getPassword(SERVICE_NAME, "tom"), "123456");
        Assert.assertNotEquals(instance.getPassword(SERVICE_NAME, "tom"), "654321");

        // has
        Map<String, String> credentials = instance.getCredentials(SERVICE_NAME);
        Assert.assertTrue(credentials.containsKey("tom"));

        // update
        instance.setPassword(SERVICE_NAME, "tom", "654321");
        Assert.assertEquals(instance.getPassword(SERVICE_NAME, "tom"), "654321");

        // has
        credentials = instance.getCredentials(SERVICE_NAME);
        Assert.assertTrue(credentials.containsKey("tom"));

        // delete
        Assert.assertTrue(instance.deletePassword(SERVICE_NAME, "tom"));
        Assert.assertNull(instance.getPassword(SERVICE_NAME, "tom"));

        // has
        credentials = instance.getCredentials(SERVICE_NAME);
        Assert.assertFalse(credentials.containsKey("tom"));

        // chinese
        // set
        instance.setPassword(SERVICE_NAME, "chinese", "你好");
        Assert.assertEquals(instance.getPassword(SERVICE_NAME, "chinese"), "你好");

        // delete
        Assert.assertTrue(instance.deletePassword(SERVICE_NAME, "chinese"));

    }

    @Test
    public void getInstance() throws Exception {

        setAndGetAndDel(Keytar.getInstance());

        if (System.getProperty("os.name", "").startsWith("Mac")) {
            setAndGetAndDel(Keytar.getInstance(KeytarTest.class, "libkeytar-mac", ".dylib"));
        }

        try {
            Keytar.getInstance(KeytarTest.class, UUID.randomUUID().toString(), ".dylib");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), NullPointerException.class);
        }

        setAndGetAndDel(Keytar.getInstance(new File(Objects
                .requireNonNull(getClass().getClassLoader().getResource("libkeytar-mac.dylib"), "libkeytar-mac.dylib")
                .getFile())));

    }

    private void setAndGetAndDel(Keytar keytar) throws KeytarException {
        final String password = UUID.randomUUID().toString();

        keytar.setPassword(SERVICE_NAME, "tom", password);

        Assert.assertEquals(keytar.getPassword(SERVICE_NAME, "tom"), password);

        Assert.assertTrue(keytar.deletePassword(SERVICE_NAME, "tom"));

        Assert.assertNull(keytar.getPassword(SERVICE_NAME, "tom"));
    }

}