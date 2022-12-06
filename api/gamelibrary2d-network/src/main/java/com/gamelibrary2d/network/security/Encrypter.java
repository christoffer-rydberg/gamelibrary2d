package com.gamelibrary2d.network.security;

import java.security.GeneralSecurityException;

public interface Encrypter {
    byte[] encrypt(byte[] plaintext) throws GeneralSecurityException;
}
