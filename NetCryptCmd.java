import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class NetCryptCmd {

    public NetCryptCmd(String[] args, boolean isClient, boolean isServer) {
        if (isClient)
        {
            startClient(args);
        }
        else if (isServer)
        {
            //startServer(args)
        }
        else
        {
            System.err.print("Invalid input occured");
        }

    }

    public void startClient(String[] args)
    {
        // Index0 = valid, Index1 = networkRequest
        HashMap<String, Boolean> parsedArgs = new HashMap<String, Boolean>(); 
        String fileName = args[args.length - 1];
        Cipher cipher;
        SecureRandom r;
        SecretKey s_key;
        IvParameterSpec IV;
        File encryptedFile;
        File decryptedFile;

        parsedArgs = parseArgs(args);

        if (parsedArgs.get("valid"))
        {
            try 
            {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                r = new SecureRandom();
                s_key = Crypto.generateKey(128, r);
                IV = Crypto.generateIV(r);

                byte[] keyBytes = s_key.getEncoded();
                byte[] ivBytes = IV.getIV();
                FileOutputStream ivOutputStream = new FileOutputStream(new File("IV.bin"));
                FileOutputStream keyOutputStream = new FileOutputStream(new File("key.bin"));

                ivOutputStream.write(ivBytes);
                keyOutputStream.write(keyBytes);

                keyOutputStream.close();
                ivOutputStream.close();

                System.out.println();
                System.out.println("N E T C R Y P T    S T A R T E D:");
                System.out.println("=================================");

                byte[] inputFileBytes = Crypto.readFile(fileName);

                int originalFileLen = inputFileBytes.length;

                byte[] messageDigest = Crypto.createMessageDigest(inputFileBytes, cipher, r, s_key, IV);
                byte[] digitalSignature = Crypto.createDigitalSignature(messageDigest, cipher, r, s_key, IV);

                encryptedFile = Crypto.encryptFile(inputFileBytes, cipher, r, s_key, IV, fileName);

                byte[] encryptedFileBytes = Crypto.readFile(encryptedFile.getPath());
                byte[] encryptedFileWithSigBytes = new byte[encryptedFileBytes.length + digitalSignature.length];



                decryptedFile = Crypto.decryptFile(encryptedFileBytes, cipher, s_key, IV, encryptedFile.getPath());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.print("Invalid options were placed in the first arguement");
        }

        Socket clientSocket = Network.createSocket("0.0.0.0", 50015, "CLIENT");

        if (clientSocket == null) {System.exit(-1);}

        try {
            clientSocket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private HashMap<String, Boolean> parseArgs(String[] args)
    {
        HashMap<String, Boolean> argParse = new HashMap<String, Boolean>();

        argParse.put("valid", false);
        argParse.put("e&a", false);
        argParse.put("se", false);

        if (args.length == 1)
        {
            argParse.put("e&A", true);
            argParse.put("valid", true);
        }
        else if (args.length == 2)
        {
            String params = args[0].toLowerCase();
            if (params.startsWith("-"))
            {
                if (params.contains("e"))
                {
                    if (params.contains("a"))
                    {
                        argParse.put("e&a", true);
                        argParse.put("valid", true);
                    }
                    else
                    {
                        argParse.put("se", true);
                        argParse.put("valid", true);
                    }
                }
            }
            else
            {
                argParse.put("valid", false);
            }
        }

        return argParse;
    }
}