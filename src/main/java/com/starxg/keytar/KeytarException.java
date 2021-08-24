package com.starxg.keytar;

/**
 * KeytarException
 *
 * @author huangxingguang
 */
public class KeytarException extends Exception {

    public KeytarException(String s) {
        super(s);
    }

    public KeytarException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public KeytarException(Throwable throwable) {
        super(throwable);
    }
}
