package org.roussev.http4e.crypt;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * A facade class hiding symmetric and asymmetric encryption algorithms.
 *
 * @author Atanas Roussev
 */

public class EncryptFacade {

    private Cipher ecipher;
    private Cipher dcipher;

    private final int SYMMETRIC = 1;
    private final int SYMMETRIC_PARAM_SPEC = 2;
    private final int SYMMETRIC_PASSPHRASE = 3;
    private final int ASYMMETRIC = 4;
    private int type = SYMMETRIC;

    private Key privateKey;
    private Key publicKey;
    private Key key;
    private AlgorithmParameterSpec parameterSpec;
    private String passPhrase;

    /**
     * EncryptFacade constructor that initializes asymmetric ciphers
     */
    public EncryptFacade(final Key privateKey, final Key publicKey) {
        type = ASYMMETRIC;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Constructor used to create this object. Responsible for setting and initializing this object's
     * encrypter and decrypter Chipher instances given a Secret Key and algorithm.
     *
     * @param key       Secret Key used to initialize both the encrypter and decrypter instances.
     * @param algorithm Which algorithm to use for creating the encrypter and decrypter instances.
     */
    public EncryptFacade(final Key key) {
        type = SYMMETRIC;
        this.key = key;
    }

    /**
     * Constructor used to create this object. Responsible for setting and initializing this object's
     * encrypter and decrypter Chipher instances given a Secret Key and AlgorithmParameterSpec.
     *
     * @param key           Secret Key used to initialize both the encrypter and decrypter instances.
     * @param parameterSpec AlgorithmParameterSpec.
     */
    public EncryptFacade(final Key key, final AlgorithmParameterSpec parameterSpec) {
        type = SYMMETRIC_PARAM_SPEC;
        this.key = key;
        this.parameterSpec = parameterSpec;
    }

    /**
     * Constructor used to create this object. Responsible for setting and initializing this object's
     * encrypter and decrypter Chipher instances given a Pass Phrase and algorithm.
     *
     * @param passPhrase Pass Phrase used to initialize both the encrypter and decrypter instances.
     */
    public EncryptFacade(final String passPhrase) {
        type = SYMMETRIC_PASSPHRASE;
        this.passPhrase = passPhrase;

        Key key = null;
        final KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), CryptConstants.SALT, CryptConstants.ITERATION_COUNT);
        try {
            key = SecretKeyFactory.getInstance(CryptConstants.SECRET_KEY_ALGORITHM).generateSecret(keySpec);
        } catch (final Exception e) {
            // System.err.println("Failed to instantiate Key of type '" + type +
            // "', passPhrase '" + passPhrase + "'");
            throw new RuntimeException(e);
        }

        // Prepare the parameters to the ciphers
        final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(CryptConstants.SALT, CryptConstants.ITERATION_COUNT);
        this.key = key;
        parameterSpec = paramSpec;
    }

    /**
     * Lazy loader
     */
    private Cipher getEncryptCypher() {
        if (ecipher != null) {
            return ecipher;
        }

        Key mKey = null;
        AlgorithmParameterSpec mParameterSpec = null;

        if (type == SYMMETRIC) {
            mKey = key;

        } else if (type == SYMMETRIC_PARAM_SPEC || type == SYMMETRIC_PASSPHRASE) {
            mKey = key;
            mParameterSpec = parameterSpec;

        } else if (type == ASYMMETRIC) {
            mKey = publicKey;

        } else {
            throw new IllegalArgumentException("EncryptFacade 'type' not provided!");
        }

        try {
            ecipher = Cipher.getInstance(mKey.getAlgorithm());
            if (mParameterSpec == null) {
                ecipher.init(Cipher.ENCRYPT_MODE, mKey);
            } else {
                ecipher.init(Cipher.ENCRYPT_MODE, mKey, mParameterSpec);
            }

        } catch (final Exception e) {
            // System.err.println("Failed to instantiate (Encrypt)Cipher of type '"
            // + type + "', key '" + Utils.getKeyInfo(key) + "', passPhrase '" +
            // passPhrase + "', paramSpec '" + parameterSpec + "'");
            throw new RuntimeException(e);
        }

        return ecipher;
    }

    /**
     * Lazy loader
     */
    private Cipher getDecryptCypher() {
        if (dcipher != null) {
            return dcipher;
        }

        Key mKey = null;
        AlgorithmParameterSpec mParameterSpec = null;

        if (type == SYMMETRIC) {
            mKey = key;

        } else if (type == SYMMETRIC_PARAM_SPEC || type == SYMMETRIC_PASSPHRASE) {
            mKey = key;
            mParameterSpec = parameterSpec;

        } else if (type == ASYMMETRIC) {
            mKey = privateKey;

        } else {
            throw new IllegalArgumentException("EncryptFacade 'type' not provided!");
        }

        try {
            dcipher = Cipher.getInstance(mKey.getAlgorithm());
            if (mParameterSpec == null) {
                dcipher.init(Cipher.DECRYPT_MODE, mKey);
            } else {
                dcipher.init(Cipher.DECRYPT_MODE, mKey, mParameterSpec);
            }

        } catch (final Exception e) {
            // System.err.println("Failed to instantiate (Decrypt)Cipher of " +
            // "type '" + type + "', key '" + Utils.getKeyInfo(key) + "',
            // passPhrase '" + passPhrase + "', paramSpec '" + parameterSpec +
            // "'");
            throw new RuntimeException(e);
        }
        return dcipher;
    }

    public byte[] encrypt(final byte[] data) {
        try {
            return getEncryptCypher().doFinal(data);

        } catch (final Exception e) {
            // System.err.println("Failed to encrypt data: '" +
            // HexUtils.prettyHex(data) + "' using CipherInfo: " +
            // Utils.getCipherInfo(getEncryptCypher()));
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes a encrypted String as an argument, decrypts and returns the decrypted String.
     *
     * @param str Encrypted String to be decrypted
     * @return <code>String</code> Decrypted version of the provided String
     */
    public byte[] decrypt(final byte[] encrData) {

        try {
            return getDecryptCypher().doFinal(encrData);

        } catch (final Exception e) {
            // System.err.println("Failed to decrypt data: '" +
            // HexUtils.prettyHex(encrData) + "' using CipherInfo: " +
            // Utils.getCipherInfo(getDecryptCypher()));
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes a single String as an argument and returns an Encrypted version of that String.
     *
     * @param str String to be encrypted
     * @return <code>String</code> Encrypted version of the provided String
     */
    public String encrypt(final String str) {
        try {
            // Encode the string into bytes using utf-8
            final byte[] utf8 = str.getBytes(CryptConstants.UTF8);

            // Encrypt
            final byte[] enc = getEncryptCypher().doFinal(utf8);

            // Encode bytes to base64 to get a string
            return new String(Base64.getEncoder().encode(enc));// new sun.misc.BASE64Encoder().encode(enc);

        } catch (final Exception e) {
            // System.err.println("Failed to encrypt: '" + str + "' using
            // CipherInfo: " + Utils.getCipherInfo(getEncryptCypher()));
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes a encrypted String as an argument, decrypts and returns the decrypted String.
     *
     * @param str Encrypted String to be decrypted
     * @return <code>String</code> Decrypted version of the provided String
     */
    public String decrypt(final String str) {

        try {

            // Decode base64 to get bytes
            final byte[] dec = Base64.getDecoder().decode(str.getBytes()); // new sun.misc.BASE64Decoder().decodeBuffer(str);

            // Decrypt
            final byte[] utf8 = getDecryptCypher().doFinal(dec);

            // Decode using utf-8
            return new String(utf8, CryptConstants.UTF8);

        } catch (final Exception e) {
            // System.err.println("Failed to decrypt '" + str + "' using
            // CipherInfo: " + Utils.getCipherInfo(getDecryptCypher()));
            throw new RuntimeException(e);
        }
    }

    static class Utils {

        static String getKeyInfo(final Key key) {
            if (key == null) {
                return "{null key}";
            }
            final StringBuilder sb = new StringBuilder(key.toString());
            sb.append("{Algorithm:" + key.getAlgorithm());
            sb.append(",Format:" + key.getFormat());
            sb.append(",Encoded:" + HexUtils.prettyHex(key.getEncoded()));
            sb.append("}");

            return sb.toString();
        }

        static String getCipherInfo(final Cipher cipher) {
            if (cipher == null) {
                return "{null cipher}";
            }
            final StringBuilder sb = new StringBuilder(cipher.toString());
            sb.append("{Algorithm:" + cipher.getAlgorithm());
            sb.append(",Provider:" + cipher.getProvider());
            sb.append(",Parameters:" + cipher.getParameters());
            sb.append(",IV:" + HexUtils.prettyHex(cipher.getIV()));
            sb.append("}");

            return sb.toString();
        }
    }

}
