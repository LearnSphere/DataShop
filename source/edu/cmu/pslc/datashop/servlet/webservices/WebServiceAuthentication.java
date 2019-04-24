/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import edu.cmu.pslc.datashop.util.StringUtils;

/**
 * Represents web services authentication credentials for a user of the system.
 * Modeled on Amazon Web Services authentication.
 *
 * @author Jim Rankin
 * @version $Revision: 12341 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-13 14:05:35 -0400 (Wed, 13 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServiceAuthentication {
    /** We perform encryption with the HMAC Sha1 algorithm. */
    private static final String HMAC_SHA1 = "HmacSHA1";

    /** upper case characters */
    private static final String UPPER = charRange(65, 26);
    /** lower case characters */
    private static final String LOWER = charRange(97, 26);
    /** digits 0 - 9 */
    private static final String DIGITS = charRange(48, 10);
    /** build API tokens from upper case letters and digits */
    private static final String API_TOKEN_CHARS = UPPER + DIGITS;
    /** build secret keys from upper and lower case letters and digits */
    private static final String SECRET_KEY_CHARS = UPPER + LOWER + DIGITS;
    /** length of an API token string */
    private static final int API_TOKEN_LEN = 20;
    /** length of a secret key */
    private static final int SECRET_KEY_LEN = 40;

    /**
     * Translate the sequence of integers from start to start + length into a string
     * containing the corresponding ASCII characters.
     * @param start the first ASCII character code
     * @param length the number of characters in the sequence
     * @return a string containing all of the characters in the sequence
     */
    private static String charRange(int start, int length) {
        StringBuffer buf = new StringBuffer(length);

        for (char i = (char)start; i < start + length; i++) {
            buf.append(i);
        }

        return buf.toString();
    }

    /**
     * Generate a new secret key.
     * @return a newly generated secret key
     */
    public static String generateSecret() {
        return StringUtils.randomString(SECRET_KEY_LEN, SECRET_KEY_CHARS);
    }

    /**
     * Generate an API token.
     * @return a newly generated API token
     */
    public static String generateApiToken() {
        return StringUtils.randomString(API_TOKEN_LEN, API_TOKEN_CHARS);
    }

    /** the secret key we will use for encryption. */
    private String secret;

    /**
     * Encapsulates authenticating a web server request.
     * @param secret the key we will use for encryption
     */
    public WebServiceAuthentication(String secret) {
        this.secret = secret;
    }

    /**
     * Encrypt the data with the secret key.
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws SignatureException if something goes wrong
     */
    private String encrypt(String data) throws SignatureException {
        String result;

        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1);

            mac.init(signingKey);

            // compute the HMAC on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the HMAC
            result = new String(Base64.encodeBase64(rawHmac, true), "UTF-8");
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }

        return result;
    }

    /**
     * Verify that the encrypted text was generated from the plain text
     * using the secret key.
     * @param plain the original text
     * @param encrypted the encrypted text
     * @return whether the encrypted text was generated from the plain text
     * using the secret key
     */
    public boolean authenticate(String plain, String encrypted) {
        if (encrypted == null) { return false; }
        try {
            return encrypted.equals(encrypt(plain));
        } catch (SignatureException e) {
            return false;
        }
    }
}
