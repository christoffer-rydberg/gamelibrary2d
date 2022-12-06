package com.gamelibrary2d.network.security;

import java.security.GeneralSecurityException;

public interface Decrypter {
    byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException;
}
