import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CryptMachine {

    public static File encryptFile(String filePath, Cipher cipher, SecureRandom r, SecretKey s_key, IvParameterSpec IV)
    {
        // Init objects
        File inputFile = new File(filePath);
        String encryptedFilePath = "NetCry_E-" + filePath;
        File encryptedFile = new File(encryptedFilePath);
        ArrayList<Byte> fileBytes = new ArrayList<Byte>();
        System.out.println("\nNETCRYPT will now encrypt " + filePath);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);
            FileOutputStream ivOutputStream = new FileOutputStream(new File("IV.bin"));
            FileOutputStream keyOutputStream = new FileOutputStream(new File("key.bin"));
            FileInputStream inputStream = new FileInputStream(inputFile);

            cipher.init(Cipher.ENCRYPT_MODE, s_key, IV, r);

            byte[] IVBytes = IV.getIV();
            ivOutputStream.write(IVBytes);
            System.out.println("Writing IV to file ("+IVBytes.length+" bytes)");

            byte[] keyBytes = s_key.getEncoded();
            keyOutputStream.write(keyBytes);
            System.out.println("Writing Key to file ("+keyBytes.length+" bytes)");

            int curByte;
            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }

            byte[] fileBytesArray = new byte[fileBytes.size()];

            System.out.println(fileBytes.size() + " bytes are being processed for encryption");

            for (int i = 0; i < fileBytesArray.length; i++)
            {
                fileBytesArray[i] = fileBytes.get(i);
            }

            byte[] encryptedBytes = cipher.doFinal(fileBytesArray);

            outputStream.write(encryptedBytes);
            System.out.println("Encrypted Bytes have been stored in " + encryptedFilePath + "\n");
            
            keyOutputStream.close();
            ivOutputStream.close();
            outputStream.close();
            inputStream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encryptedFile;
    }

    public static File decryptFile(String filePath, Cipher cipher, SecretKey s_key, IvParameterSpec IV)
    {
        File inputFile = new File(filePath);
        String decryptedFilePath = "NetCry_D-" + filePath.substring(9);
        File decryptedFile = new File(decryptedFilePath);
        ArrayList<Byte> fileBytes = new ArrayList<Byte>();
        System.out.println("NETCRYPT will now decrypt " + filePath);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream(decryptedFile);
            FileInputStream inputStream = new FileInputStream(inputFile);

            cipher.init(Cipher.DECRYPT_MODE, s_key, IV);

            int curByte;
            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }

            byte[] fileBytesArray = new byte[fileBytes.size()];

            System.out.println(fileBytes.size() + " bytes are being processed for decryption");

            for (int i = 0; i < fileBytesArray.length; i++)
            {
                fileBytesArray[i] = fileBytes.get(i);
            }

            byte[] decryptedBytes = cipher.doFinal(fileBytesArray);

            outputStream.write(decryptedBytes);
            System.out.println("Decrypted Bytes have been stored in " + decryptedFilePath + "\n");

            outputStream.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return decryptedFile;

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