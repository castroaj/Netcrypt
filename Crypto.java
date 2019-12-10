
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {

    public static byte[] encryptBytes(byte[] fileBytesArray, Cipher cipher, SecureRandom r, SecretKey s_key, IvParameterSpec IV)
    {
        byte[] encryptedBytes = null;
        try 
        {
            cipher.init(Cipher.ENCRYPT_MODE, s_key, IV, r);
            encryptedBytes = cipher.doFinal(fileBytesArray);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encryptedBytes;
    }

    public static byte[] decryptBytes(byte[] fileBytesArray, Cipher cipher, SecretKey s_key, IvParameterSpec IV)
    {
        byte[] decryptedBytes = null;
        try 
        {
            cipher.init(Cipher.DECRYPT_MODE, s_key, IV);
            decryptedBytes = cipher.doFinal(fileBytesArray);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return decryptedBytes;
    }

    public static byte[] createMessageDigest(byte[] fileBytes)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fileBytes);
            return digest.digest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }

    public static SecretKey generateKey(int keySize, SecureRandom r) throws NoSuchAlgorithmException 
    {
        byte[] newSeed = r.generateSeed(32);
        r.setSeed(newSeed);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom sRandom = r.getInstanceStrong();

        keyGen.init(keySize, sRandom);

        SecretKey s_key = keyGen.generateKey();

        return s_key;
    }

    public static IvParameterSpec generateIV(SecureRandom r) throws NoSuchAlgorithmException
    {
        byte[] newSeed = r.generateSeed(16);
        r.setSeed(newSeed);

        byte[] byteIV = new byte[16];

        r.nextBytes(byteIV);

        IvParameterSpec IV = new IvParameterSpec(byteIV);
        return IV;
    }

    public static void printDigest(byte[] messageDigest)
    {
        for (int i = 0; i < messageDigest.length; i++)
        {
            if (i % 16 == 0 && i != 0)
            {
                System.out.println();
            }
            System.out.printf("%x ", messageDigest[i]);
        }

        System.out.println("\n");
    }

    

}



// // Segment Size is 1024
            // int segmentCount = fileBytes.size() / 1024;
            // int lastSegSize = fileBytes.size() % 1024;

            // System.out.println("\nNETCRYPT has found "+ fileBytes.size() + " bytes\nWhich will be divided into " + segmentCount + " segments of 1024 bytes\nWith " + lastSegSize + " bytes left in the final segment");

            // // Encrypt each segment of the file and add it to the encryptedByteArrayList
            // for (int i = 0; i < segmentCount; i++)
            // {
            //     byte[] buffer = new byte[1024];
            //     int byteOffset = i * 1024;

            //     int bIndex = 0;
            //     for (int j = byteOffset; j < (byteOffset + 1024); j++)
            //     {
            //         buffer[bIndex] = fileBytes.get(j);
            //         bIndex++; 
            //     }

            //     byte[] encryptedBuffer = cipher.update(buffer);

            //     for (int a = 0; a < 1024; a++)
            //      {
            //          encryptedFileBytes.add(encryptedBuffer[a]);
            //      }
            // }

            // //Encrypt final section of the data
            // byte[] buffer = new byte[1024];
            // int bIndex = 0;
            // for (int i = segmentCount * 1024; i < fileBytes.size(); i++)
            // {
            //     buffer[bIndex] = fileBytes.get(i);
            //     bIndex++;
            // }
            // byte[] finalBuffer = cipher.doFinal(buffer);
            // for (int a = 0; a < 1024; a++)
            // {
            //     encryptedFileBytes.add(finalBuffer[a]);
            // }

            // byte[] encryptedFileBytesArray = new byte[encryptedFileBytes.size()];
            // for (int i = 0; i < encryptedFileBytesArray.length; i++)
            // {
            //     encryptedFileBytesArray[i] = encryptedFileBytes.get(i);
            // }