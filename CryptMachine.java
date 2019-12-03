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

    private File inputFile;
    private File encryptedFile;
    private String encryptedFilePath;
   
    private FileInputStream inputStream;
    private FileOutputStream outputStream;

    private ArrayList<Byte> fileBytes;
    private ArrayList<Byte> encryptedFileBytes;

    private SecretKey s_key;
    private IvParameterSpec IV;
    private Cipher cipher;
    private SecureRandom r;

    public File encryptFile(String filePath)
    {
        // Init objects
        this.inputFile = new File(filePath);
        this.encryptedFilePath = "NetCry-" + filePath;
        this.encryptedFile = new File(encryptedFilePath);
        this.fileBytes = new ArrayList<Byte>();
        this.encryptedFileBytes = new ArrayList<Byte>();
        System.out.println("\nNETCRYPT will now encrypt " + filePath);

        try 
        {
            outputStream = new FileOutputStream(encryptedFile);
            inputStream = new FileInputStream(inputFile);

            //Init Encryption objects
            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.r = new SecureRandom();
            this.s_key = generateKey(128);
            this.IV = generateIV();
            cipher.init(Cipher.ENCRYPT_MODE, this.s_key, this.IV, this.r);

            byte[] IVBytes = IV.getIV();

            for (int i = 0; i < 16; i++)
            {
                encryptedFileBytes.add(IVBytes[i]);
            }

            int curByte;
            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }

            // Segment Size is 1024
            int segmentCount = fileBytes.size() / 1024;
            int lastSegSize = fileBytes.size() % 1024;

            System.out.println("\nNETCRYPT has found "+ fileBytes.size() + " bytes\nWhich will be divided into " + segmentCount + " segments of 1024 bytes\nWith " + lastSegSize + " bytes left in the final segment");

            // Encrypt each segment of the file and add it to the encryptedByteArrayList
            for (int i = 0; i < segmentCount; i++)
            {
                byte[] buffer = new byte[1024];
                int byteOffset = i * 1024;

                int bIndex = 0;
                for (int j = byteOffset; j < (byteOffset + 1024); j++)
                {
                    buffer[bIndex] = fileBytes.get(j);
                    bIndex++; 
                }

                byte[] encryptedBuffer = cipher.update(buffer);

                for (int a = 0; a < 1024; a++)
                 {
                     encryptedFileBytes.add(encryptedBuffer[a]);
                 }
            }

            //Encrypt final section of the data
            byte[] buffer = new byte[1024];
            int bIndex = 0;
            for (int i = segmentCount * 1024; i < fileBytes.size(); i++)
            {
                buffer[bIndex] = fileBytes.get(i);
                bIndex++;
            }
            byte[] finalBuffer = cipher.doFinal(buffer);
            for (int a = 0; a < 1024; a++)
            {
                encryptedFileBytes.add(finalBuffer[a]);
            }

            byte[] encryptedFileBytesArray = new byte[encryptedFileBytes.size()];
            for (int i = 0; i < encryptedFileBytesArray.length; i++)
            {
                encryptedFileBytesArray[i] = encryptedFileBytes.get(i);
            }

            outputStream.write(encryptedFileBytesArray);
            System.out.print("\nEncrypted Bytes have been stored in " + encryptedFilePath);
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encryptedFile;
    } 


    private SecretKey generateKey(int keySize) throws NoSuchAlgorithmException 
    {
        byte[] newSeed = r.generateSeed(32);
        r.setSeed(newSeed);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom sRandom = r.getInstanceStrong();

        keyGen.init(keySize, sRandom);

        s_key = keyGen.generateKey();

        return s_key;
    }

    private IvParameterSpec generateIV() throws NoSuchAlgorithmException
    {
        byte[] newSeed = r.generateSeed(16);
        r.setSeed(newSeed);

        byte[] byteIV = new byte[16];
        r.nextBytes(byteIV);
        IV = new IvParameterSpec(byteIV);
        return IV;
    }

}