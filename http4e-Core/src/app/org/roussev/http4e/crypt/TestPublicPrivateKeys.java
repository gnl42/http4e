package org.roussev.http4e.crypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ----------------------------------------------------------------------------- The following
 * example generates a key pair for various public/private key algorithms. Some of the more popular
 * algorithms are: 1.2.840.10040.4.1 1.3.14.3.2.12 DH DSA DiffieHellman OID.1.2.840.10040.4.1 RSA
 * -----------------------------------------------------------------------------
 */

public class TestPublicPrivateKeys {

    private static void generateKeys(final String keyAlgorithm, final int numBits) {

        try {
            // Get the public/private key pair
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
            keyGen.initialize(numBits);
            final KeyPair keyPair = keyGen.genKeyPair();
            final PrivateKey privateKey = keyPair.getPrivate();
            final PublicKey publicKey = keyPair.getPublic();

            System.out.println("\n" + "Generating key/value pair using " + privateKey.getAlgorithm() + " algorithm");

            // Get the bytes of the public and private keys
            final byte[] privateKeyBytes = privateKey.getEncoded();
            final byte[] publicKeyBytes = publicKey.getEncoded();

            // Get the formats of the encoded bytes
            final String formatPrivate = privateKey.getFormat(); // PKCS#8
            final String formatPublic = publicKey.getFormat(); // X.509

            System.out.println("  Private Key Format : " + formatPrivate);
            System.out.println("  Public Key Format  : " + formatPublic);

            // The bytes can be converted back to public and private key objects
            final KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            final PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);

            final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            final PublicKey publicKey2 = keyFactory.generatePublic(publicKeySpec);

            // The original and new keys are the same
            System.out.println("  Are both private keys equal? " + privateKey.equals(privateKey2));

            System.out.println("  Are both public keys equal? " + publicKey.equals(publicKey2));

        } catch (final InvalidKeySpecException specException) {

            System.out.println("Exception");
            System.out.println("Invalid Key Spec Exception");

        } catch (final NoSuchAlgorithmException e) {

            System.out.println("Exception");
            System.out.println("No such algorithm: " + keyAlgorithm);

        }

    }

//    public static void main(String[] args) {
//
//        // Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
//        generateKeys("DSA", 1024);
//
//        // Generate a 576-bit DH key pair
//        generateKeys("DH", 576);
//
//        // Generate a 1024-bit RSA key pair
//        generateKeys("RSA", 1024);
//    }

}