import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {

    public static File encryptFile(byte[] fileBytesArray, Cipher cipher, SecureRandom r, SecretKey s_key, IvParameterSpec IV, String filePath)
    {
        String encryptedFilePath = "NetCry_E-" + filePath;
        File encryptedFile = new File(encryptedFilePath);
        System.out.println("\nNETCRYPT will now encrypt " + filePath);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);
            cipher.init(Cipher.ENCRYPT_MODE, s_key, IV, r);
            byte[] encryptedBytes = cipher.doFinal(fileBytesArray);

            outputStream.write(encryptedBytes);
            System.out.println("Encrypted Bytes have been stored in " + encryptedFilePath + "\n");
            
            outputStream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encryptedFile;
    }

    public static File decryptFile(byte[] fileBytesArray, Cipher cipher, SecretKey s_key, IvParameterSpec IV, String filePath)
    {
        String decryptedFilePath = "NetCry_D-" + filePath.substring(9);
        File decryptedFile = new File(decryptedFilePath);
        System.out.println("NETCRYPT will now decrypt " + filePath);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream(decryptedFile);
            cipher.init(Cipher.DECRYPT_MODE, s_key, IV);
            byte[] decryptedBytes = cipher.doFinal(fileBytesArray);

            outputStream.write(decryptedBytes);
            System.out.println("Decrypted Bytes have been stored in " + decryptedFilePath + "\n");

            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return decryptedFile;
    }

    public static byte[] createMessageDigest(byte[] fileBytes, Cipher c, SecureRandom r, SecretKey s_key, IvParameterSpec IV)
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

    public static byte[] createDigitalSignature(byte[] digest, Cipher c, SecureRandom r, SecretKey s_key, IvParameterSpec IV)
    {
        try {
            c.init(Cipher.ENCRYPT_MODE, s_key, IV, r);
            return c.doFinal(digest);
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

    public static byte[] readFile (String fileName)
    {
        
        ArrayList<Byte> fileBytes = new ArrayList<Byte>();

        try {
            int curByte;
            FileInputStream inputStream = new FileInputStream(new File(fileName));
            
            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        byte[] fileBytesArray = new byte[fileBytes.size()];

        System.out.println(fileBytes.size() + " bytes are being processed for encryption");

        for (int i = 0; i < fileBytesArray.length; i++)
        {
            fileBytesArray[i] = fileBytes.get(i);
        }

        return fileBytesArray;
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