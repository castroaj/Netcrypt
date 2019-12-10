import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jdk.jshell.execution.Util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

        Socket clientSocket = Network.createSocket(args[0], Integer.parseInt(args[1]), "CLIENT");

        if (clientSocket == null) {System.exit(-1);}


        if (parsedArgs.get("valid"))
        {
            try 
            {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                r = new SecureRandom();
                s_key = Crypto.generateKey(128, r);
                IV = Crypto.generateIV(r);

                System.out.println();
                System.out.println("N E T C R Y P T   C L I E N T   S T A R T E D:");
                System.out.println("=================================");
                
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("Sending Sync Message to NetCryptServer Application\n");
                out.writeInt(2048);

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
                
                byte[] lengthsRSA = new byte[2];

                lengthsRSA[0] = (byte) keyBytes.length;
                lengthsRSA[1] = (byte) ivBytes.length;

                byte[] keyAndIvBytesWithLens = Utilities.combineArrays(lengthsRSA, keyAndIvBytes);

                byte[] rsaEncryptedMsg = rsa.encrypt(keyAndIvBytesWithLens);


                System.out.println("\nSending RSA encrypted message to server");
                out.writeInt(rsaEncryptedMsg.length);
                out.write(rsaEncryptedMsg);

                byte[] inputFileBytes = Utilities.readFile(fileName);
                byte[] messageDigest = Crypto.createMessageDigest(inputFileBytes);

                byte[] inputFileWithDigest = Utilities.combineArrays(inputFileBytes, messageDigest);

                byte[] inputLen = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(inputFileBytes.length).array();
                byte[] digestLen = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(messageDigest.length).array();

                System.out.println("The file len is: " + inputFileBytes.length);

                byte[] symLens = Utilities.combineArrays(inputLen, digestLen);

                byte[] inputFileWithDigestAndLengths = Utilities.combineArrays(symLens, inputFileWithDigest);
                byte[] encryptedFileBytes = Crypto.encryptBytes(inputFileWithDigestAndLengths, cipher, r, s_key, IV);

                // for (int i =0; i < inputFileWithDigestAndLengths.length; i++)
                // {
                //     System.out.print(inputFileWithDigestAndLengths[i] + " ");
                // }

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

        ServerSocket servSocket = Network.createServerSocket(Integer.parseInt(args[0]), "Server");

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

            byte[] symDecryptedBytesWithHeaders = Crypto.decryptBytes(symEncryptedMsg, cipher, s_key, IV);

            int fileLen = 0;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                fileLen += (symDecryptedBytesWithHeaders[i] & 0x000000FF) << shift;
            }
            int digestLen = 0;
            int j = 4;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                digestLen += (symDecryptedBytesWithHeaders[j] & 0x000000FF) << shift;
                j++;
            }

            byte[] symDecryptedBytes = new byte[fileLen];
            byte[] digestBytes = new byte[digestLen];

            System.arraycopy(symDecryptedBytesWithHeaders, 8, symDecryptedBytes, 0, fileLen);
            System.arraycopy(symDecryptedBytesWithHeaders, (8 + fileLen), digestBytes, 0, digestLen);

            byte[] locallyComputedDigest = Crypto.createMessageDigest(symDecryptedBytes);

            boolean valid = Arrays.equals(locallyComputedDigest, digestBytes);    

            for (int i = 0; i < locallyComputedDigest.length; i++)
            {
                System.out.print(locallyComputedDigest[i]);
            }
            System.out.print("\n\n\n\n");
            for (int i = 0; i < digestBytes.length; i++)
            {
                System.out.print(digestBytes[i]);
            }

            System.out.println(valid);

            Utilities.writeFile(symDecryptedBytes, "DecryptedFile.txt");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private HashMap<String, Boolean> parseArgs(String[] args)
    {
        HashMap<String, Boolean> argParse = new HashMap<String, Boolean>();

        argParse.put("valid", false);

        if (args.length == 3)
        {
            argParse.put("valid", true);
        }

        return argParse;
    }
}
