package com.starxg.keytar;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Keytar
 *
 * @author huangxingguang
 */
public class Keytar {

    private static volatile Keytar instance;

    private Keytar() {
    }

    public static Keytar getInstance() {
        if (instance == null) {
            return getInstance(Keytar.class, "libkeytar", getSuffix());
        }
        return instance;
    }

    public static Keytar getInstance(Class<?> clazz, String classpathLibraryName, String libraryNameSuffix) {

        if (instance == null) {
            synchronized (Keytar.class) {
                if (instance == null) {
                    try {

                        // Stream the library out of the JAR
                        final File tmpFile = File.createTempFile(classpathLibraryName, libraryNameSuffix);
                        try (InputStream is = Objects.requireNonNull(
                                clazz.getClassLoader().getResourceAsStream(classpathLibraryName + libraryNameSuffix),
                                classpathLibraryName + libraryNameSuffix + " not found");
                             OutputStream os = new FileOutputStream(tmpFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) > 0) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }

                        return getInstance(tmpFile);

                    } catch (IOException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
            }
        }

        return instance;

    }

    public static Keytar getInstance(File absoluteLibraryFile) {
        if (instance == null) {
            synchronized (Keytar.class) {
                if (instance == null) {
                    loadSharedObject(absoluteLibraryFile);
                    instance = new Keytar();
                }
            }
        }
        return instance;
    }

    private native String _getPassword(String service, String account) throws KeytarException;

    private native void _setPassword(String service, String account, String password) throws KeytarException;

    private native boolean _deletePassword(String service, String account) throws KeytarException;

    private native Map<String, String> _getCredentials(String service) throws KeytarException;

    public String getPassword(String service, String account) throws KeytarException {
        return _getPassword(Objects.requireNonNull(service, "service must not be null"),
                Objects.requireNonNull(account, "account must not be null"));
    }

    public void setPassword(String service, String account, String password) throws KeytarException {
        _setPassword(Objects.requireNonNull(service, "service must not be null"),
                Objects.requireNonNull(account, "account must not be null"),
                Objects.requireNonNull(password, "password must not be null"));
    }

    public boolean deletePassword(String service, String account) throws KeytarException {
        return _deletePassword(Objects.requireNonNull(service, "service must not be null"),
                Objects.requireNonNull(account, "account must not be null"));
    }

    public Map<String, String> getCredentials(String service) throws KeytarException {
        Map<String, String> credentials = _getCredentials(Objects.requireNonNull(service, "service must not be null"));
        return credentials == null ? Collections.emptyMap() : credentials;
    }

    private static void loadSharedObject(File absoluteLibraryFile) {
        System.load(absoluteLibraryFile.getAbsolutePath());
    }

    private static String getSuffix() {

        String osArch = System.getProperty("os.arch");
        String cpuArch = null;

        if (osArch != null) {
            osArch = osArch.toLowerCase();
            if ("amd64".equalsIgnoreCase(osArch) || "x86_64".equalsIgnoreCase(osArch)) {
                cpuArch = "x64";
            } else if ("x86".equalsIgnoreCase(osArch)) {
                cpuArch = "x86";
            } else if ("aarch64".equals(osArch) || osArch.startsWith("armv8") || osArch.startsWith("arm64")) {
                // 64-bit ARM
                cpuArch = "arm64";
            }
        }

        if (cpuArch == null) {
            String sunArch = System.getProperty("sun.arch.data.model");
            if ("64".equals(sunArch)) {
                cpuArch = "x64";
            } else if ("32".equals(sunArch)) {
                cpuArch = "x86";
            }
        }

        final String osName = System.getProperty("os.name");

        if (cpuArch == null) {
            throw new IllegalStateException(
                    String.format("os.arch unknown, please report this to us: os.arch=%s, model=%s, os.name=%s",
                            System.getProperty("os.arch"), System.getProperty("sun.arch.data.model"), osName));
        }

        if (osName.startsWith("Win")) {
            cpuArch = "-win32-" + cpuArch + ".dll";
        } else if (osName.startsWith("Mac")) {
            cpuArch = "-darwin-" + cpuArch + ".dylib";
        } else if (osName.startsWith("Linux")) {
            cpuArch = "-linux-" + cpuArch + ".so";
        }

        return cpuArch;
    }

}
