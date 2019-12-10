import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import javafx.scene.chart.PieChart.Data;

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
            startServer(args);
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

        parsedArgs = parseArgs(args);

        Socket clientSocket = Network.createSocket("127.0.0.1", 50015, "CLIENT");

        if (clientSocket == null) {System.exit(-1);}


        if (parsedArgs.get("valid"))
        {
            try 
            {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                r = new SecureRandom();
                s_key = Crypto.generateKey(128, r);
                IV = Crypto.generateIV(r);

                // byte[] keyBytes = s_key.getEncoded();
                // byte[] ivBytes = IV.getIV();
                // FileOutputStream ivOutputStream = new FileOutputStream(new File("IV.bin"));
                // FileOutputStream keyOutputStream = new FileOutputStream(new File("key.bin"));

                // ivOutputStream.write(ivBytes);
                // keyOutputStream.write(keyBytes);

                // keyOutputStream.close();
                // ivOutputStream.close();

                System.out.println();
                System.out.println("N E T C R Y P T   C L I E N T   S T A R T E D:");
                System.out.println("=================================");
                
                //decryptedFile = Crypto.decryptFile(encryptedFileBytes, cipher, s_key, IV, encryptedFile.getPath());

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("Sending Sync Message to NetCryptServer Application\n");
                out.writeInt(2048);
                //out.close();

                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                int eLen = in.readInt();
                int nLen = in.readInt();
                int publicKeyLen = in.readInt();
                byte[] publicKey = new byte[publicKeyLen];
                in.read(publicKey);

                System.out.println("Receieved encryption Key length from server: "+ eLen);
                System.out.println("Receieved prime length from server: "+ nLen);
                System.out.println("Receieved Public Key length from server: "+ publicKeyLen);
                System.out.println("Receieved Public Key from server");
                
                byte[] eBytes = new byte[eLen];
                byte[] nBytes = new byte[nLen];

                System.arraycopy(publicKey, 0, eBytes, 0, eLen);
                System.arraycopy(publicKey, eBytes.length, nBytes, 0, nLen);

                BigInteger e = new BigInteger(eBytes);
                BigInteger n = new BigInteger(nBytes);

                RSA rsa = new RSA(e, n);

                byte[] keyBytes = s_key.getEncoded();
                byte[] ivBytes = IV.getIV();

                byte[] keyAndIvBytes = Utilities.combineArrays(keyBytes, ivBytes);
                
                byte[] lengths = new byte[2];

                lengths[0] = (byte) keyBytes.length;
                lengths[1] = (byte) ivBytes.length;

                byte[] keyAndIvBytesWithLens = Utilities.combineArrays(lengths, keyAndIvBytes);

                byte[] rsaEncryptedMsg = rsa.encrypt(keyAndIvBytesWithLens);


                System.out.println("\nSending RSA encrypted message to server");
                out.writeInt(rsaEncryptedMsg.length);
                out.write(rsaEncryptedMsg);

                byte[] inputFileBytes = Utilities.readFile(fileName);
                byte[] messageDigest = Crypto.createMessageDigest(inputFileBytes, cipher, r, s_key, IV);

                byte[] inputFileWithDigest = Utilities.combineArrays(inputFileBytes, messageDigest);

                byte[] encryptedFileBytes = Crypto.encryptBytes(inputFileWithDigest, cipher, r, s_key, IV);


                out.writeInt(encryptedFileBytes.length);
                out.write(encryptedFileBytes);

                //wait();

                //clientSocket.close();
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        else
        {
            System.err.print("Invalid options were placed in the first arguement");
        }
    }

    public void startServer(String[] args)
    {

        System.out.println();
        System.out.println("N E T C R Y P T    S E R V E R    S T A R T E D:");
        System.out.println("=================================");

        ServerSocket servSocket = Network.createServerSocket(50015, "Server");

        try 
        {
            Socket recSocket = servSocket.accept();
            DataInputStream in = new DataInputStream(recSocket.getInputStream());

            int sync = in.readInt();

            if (sync == 2048 || sync == 1024)
            {
                System.out.println("Recieved sync message from client");
            }
            else
            {
                System.exit(-1);
            }

            RSA rsa = new RSA(sync);

            byte[] publicKey = rsa.getPublicKey();
            int publicKeyLen = publicKey.length;

            DataOutputStream out = new DataOutputStream(recSocket.getOutputStream());

            System.out.println("Sending RSA public key to client:");
            System.out.println("\tencryption key length: " + rsa.getELen());
            System.out.println("\tprime length: " + rsa.getNLen());
            System.out.println("\tpublic key length: " + publicKeyLen);

            out.writeInt(rsa.getELen());
            out.writeInt(rsa.getNLen());

            out.writeInt(publicKeyLen);
            out.write(publicKey);

            int rsaEncryptedMsgLen = in.readInt();
            byte[] rsaEncryptedMsg = new byte[rsaEncryptedMsgLen];
            in.read(rsaEncryptedMsg);

            System.out.println("\n\nReceived encryptedMsg from client: " + rsaEncryptedMsgLen);

            byte[] rsaDecryptedMsg = rsa.decrypt(rsaEncryptedMsg);

            int keyLen = rsaDecryptedMsg[0];
            int ivLen = rsaDecryptedMsg[1];

            byte[] keyBytes = new byte[keyLen];
            byte[] ivBytes = new byte[ivLen];

            System.arraycopy(rsaDecryptedMsg, 2, keyBytes, 0, keyLen);
            System.arraycopy(rsaDecryptedMsg, keyLen + 2, ivBytes, 0, ivLen);

            SecretKey s_key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            IvParameterSpec IV = new IvParameterSpec(ivBytes);

            int symEncryptedMsgLen = in.readInt();
            byte[] symEncryptedMsg = new byte[symEncryptedMsgLen];

            in.read(symEncryptedMsg);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] decryptedBytes = Crypto.decryptBytes(symEncryptedMsg, cipher, s_key, IV);

            Utilities.writeFile(decryptedBytes, "DecryptedFile.txt");


            // int length = in.readInt();
            // if (length > 0)
            // {
            //     byte[] file = new byte[length];
            //     int x = in.read(file);
            //     for (int i = 0; i < file.length; i++)
            //     {
            //         System.out.print(file[i] + " ");
            //     }
            // }
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
