# java-keytar

[keytar](https://github.com/atom/node-keytar) bindings for Java

A native Java module to get, add, replace, and delete passwords in system's keychain. On macOS the passwords are managed by the Keychain, on Linux they are managed by the Secret Service API/libsecret, and on Windows they are managed by Credential Vault.

## Usage
```xml
<dependency>
  <groupId>com.starxg</groupId>
  <artifactId>java-keytar</artifactId>
  <version>1.0.0</version>
</dependency>
```

```java
Keytar instance = Keytar.getInstance();

// set/update
instance.setPassword("your service name", "tom", "123456");
// get
instance.getPassword("your service name", "tom");
// delete
instance.deletePassword("your service name", "tom");
// get all
instance.getCredentials("your service name");
```

## Linux Requirement

Currently this library uses `libsecret`. Depending on your distribution,
you will need to install the appropriate package, e.g.:

- Debian/Ubuntu: `sudo apt-get install libsecret-1-dev`
- Red Hat-based: `sudo yum install libsecret-devel`
- Arch Linux: `sudo pacman -S libsecret`
