import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

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
import javax.crypto.Cipher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetCrypt {

    public NetCrypt(String[] args, boolean isClient, boolean isServer) {
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
        HashMap<String, Boolean> parsedArgs = new HashMap<String, Boolean>(); 
        String fileName = args[args.length - 1];
        Cipher cipher;
        SecureRandom r;
        SecretKey s_key;
        IvParameterSpec IV;
        int rsaBitLength = 2048;

        parsedArgs = parseArgs(args);

        System.out.println();
        System.out.println("N E T C R Y P T   C L I E N T   S T A R T E D:");
        System.out.println("==============================================");

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
                
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("Sending Sync Message to Server\n");
                System.out.println("Waiting for response from Server...");
                out.writeInt(rsaBitLength);

                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                int eLen = in.readInt();
                int nLen = in.readInt();
                int publicKeyLen = in.readInt();
                byte[] publicKey = new byte[publicKeyLen];
                in.read(publicKey);

                System.out.println("Receieved public RSA Key from server:");
                
                byte[] eBytes = new byte[eLen];
                byte[] nBytes = new byte[nLen];

                System.arraycopy(publicKey, 0, eBytes, 0, eLen);
                System.arraycopy(publicKey, eBytes.length, nBytes, 0, nLen);

                BigInteger e = new BigInteger(eBytes);
                BigInteger n = new BigInteger(nBytes);

                System.out.println("\nRSA public Encryption key: ("+ e.bitLength() +" bits)\n" + e.toString());
                System.out.println("\nRSA prime: ("+ n.bitLength() +" bits)\n" + n.toString());

                RSA rsa = new RSA(e, n);

                byte[] keyBytes = s_key.getEncoded();
                byte[] ivBytes = IV.getIV();

                byte[] keyAndIvBytes = Utilities.combineArrays(keyBytes, ivBytes);
                
                byte[] lengthsRSA = new byte[2];

                lengthsRSA[0] = (byte) keyBytes.length;
                lengthsRSA[1] = (byte) ivBytes.length;

                byte[] keyAndIvBytesWithLens = Utilities.combineArrays(lengthsRSA, keyAndIvBytes);
                byte[] rsaEncryptedMsg = rsa.encrypt(keyAndIvBytesWithLens);

                System.out.println("\nPackaging Symmetric Key and Inital Vector into message for server");
                System.out.println("Encrypting Msg for server with RSA");
                System.out.println("Sending RSA encrypted message to server");
                out.writeInt(rsaEncryptedMsg.length);
                out.write(rsaEncryptedMsg);

                System.out.println("\nPreparing to read INPUTFILE");

                byte[] inputFileBytes = Utilities.readFile(fileName);


                System.out.println("\nComputing SHA-256 Digest for INPUTFILE...");
                byte[] messageDigest = Crypto.createMessageDigest(inputFileBytes);

                System.out.println("\nSHA256 Digest: ("+ messageDigest.length +" bytes)");
                Crypto.printDigest(messageDigest);

                byte[] inputFileWithDigest = Utilities.combineArrays(inputFileBytes, messageDigest);

                byte[] inputLen = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(inputFileBytes.length).array();
                byte[] digestLen = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(messageDigest.length).array();

                byte[] symLens = Utilities.combineArrays(inputLen, digestLen);

                byte[] inputFileWithDigestAndLengths = Utilities.combineArrays(symLens, inputFileWithDigest);
                byte[] encryptedFileBytes = Crypto.encryptBytes(inputFileWithDigestAndLengths, cipher, r, s_key, IV);


                System.out.println("Packaging INPUTFILE with attached digest into message for server");
                System.out.println("Encrypting Msg for server with AES/CBC/PKCS5Padding");
                System.out.println("Sending AES encrypted message to server");

                out.writeInt(encryptedFileBytes.length);
                out.write(encryptedFileBytes);

                // InputStream inStream = new ByteArrayInputStream(encryptedFileBytes);
                // BufferedInputStream buffIn = new BufferedInputStream(inStream);
                // BufferedOutputStream buffOut = new BufferedOutputStream(clientSocket.getOutputStream());

                // byte[] buffer = new byte[1024];
                // int len = 0;
                // int index = 0;
                // while ((len = buffIn.read(buffer)) > 0)
                // {
                //     buffOut.write(buffer, 0, len);
                //     System.out.println(index);
                //     index += len;
                // }
                

                System.out.println("\nWaiting for Server to acknowledge file transmission...");

                boolean validAcknowledgement = in.readBoolean();

                System.out.println("Acknowledgement from server was recieved");

                if (validAcknowledgement)
                {
                    System.out.println("\nSHA-256 digest was validated on serverside. File was successfully transmitted\n");
                }
                else
                {
                    System.out.println("\nSHA-256 digest was NOT validated on serverside. File was discarded\n");
                }

                clientSocket.close();
                in.close();
                out.close();
                
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
        System.out.println("================================================");

        ServerSocket servSocket = Network.createServerSocket(Integer.parseInt(args[0]), "Server");

        System.out.println("Waiting for request from client...");

        try 
        {
            Socket recSocket = servSocket.accept();
            DataInputStream in = new DataInputStream(recSocket.getInputStream());

            int sync = in.readInt();

            if (sync == 2048 || sync == 1024 || sync == 512 || sync == 3072)
            {
                System.out.println("\nRecieved sync message from client");
            }
            else
            {
                System.exit(-1);
            }

            System.out.println("Creating RSA prime and key pair...");

            RSA rsa = new RSA(sync);

            byte[] publicKey = rsa.getPublicKey();
            int publicKeyLen = publicKey.length;

            DataOutputStream out = new DataOutputStream(recSocket.getOutputStream());

            System.out.println("\nSending RSA public key to client:");
            System.out.println("RSA public Encryption key: ("+ rsa.getE().bitLength() +" bits)\n" +rsa.getE().toString());
            System.out.println("\nRSA prime: ("+ rsa.getN().bitLength() +" bits)\n" + rsa.getN().toString());

            out.writeInt(rsa.getELen());
            out.writeInt(rsa.getNLen());

            out.writeInt(publicKeyLen);
            out.write(publicKey);

            System.out.println("\nWaiting to recieve RSA encrypted Msg from client...");

            int rsaEncryptedMsgLen = in.readInt();
            byte[] rsaEncryptedMsg = new byte[rsaEncryptedMsgLen];
            in.read(rsaEncryptedMsg);

            System.out.println("\nReceived RSA encrypted message from client");

            byte[] rsaDecryptedMsg = rsa.decrypt(rsaEncryptedMsg);

            System.out.println("Decrypting RSA encrypted message.....");

            int keyLen = rsaDecryptedMsg[0];
            int ivLen = rsaDecryptedMsg[1];

            byte[] keyBytes = new byte[keyLen];
            byte[] ivBytes = new byte[ivLen];

            System.arraycopy(rsaDecryptedMsg, 2, keyBytes, 0, keyLen);
            System.arraycopy(rsaDecryptedMsg, keyLen + 2, ivBytes, 0, ivLen);

            System.out.println("\nAES Symmetric Key and IV have been decrypted");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey s_key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            IvParameterSpec IV = new IvParameterSpec(ivBytes);

            System.out.println("Waiting to recieve AES encrypted message from client...");

            int symEncryptedMsgLen = in.readInt();
            byte[] recievedBytes = new byte[symEncryptedMsgLen];

            in.read(recievedBytes);

            // BufferedInputStream buffIn = new BufferedInputStream(recSocket.getInputStream());
            
            // byte[] recievedBytes = new byte[symEncryptedMsgLen];

            // byte[] buffer = new byte[1024];
            // int len;
            // int index = 0;
            // while ((len = buffIn.read(buffer)) > 0)
            // {
            //     System.arraycopy(buffer, 0, recievedBytes, index, len);
            //     System.out.println(len);
            //     index += len;
            // }

            // buffIn.close();
            // System.out.flush();

            System.out.println("Recieved AES encrypted message from client");
            System.out.println("\nDecrypting AES encrypted message.....");

            byte[] symDecryptedBytesWithHeaders = Crypto.decryptBytes(recievedBytes, cipher, s_key, IV);

            System.out.println("INPUTFILE with digest have been decrypted");

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

            System.out.println("Detaching Digest from INPUTFILE...");
            System.out.println("\nRecieved SHA-256 Digest: ("+digestBytes.length+" bytes)");
            Crypto.printDigest(digestBytes);

            byte[] locallyComputedDigest = Crypto.createMessageDigest(symDecryptedBytes);

            System.out.println("Locally Computing Digest on recieved file for comparison...");
            System.out.println("\nLocally computed SHA-256 Digest: ("+locallyComputedDigest.length+" bytes)");
            Crypto.printDigest(locallyComputedDigest);

            System.out.println("Comparing Digests for authentication...");
            System.out.print("The SHA-256 Digest is ");

            boolean valid = Arrays.equals(locallyComputedDigest, digestBytes);    

            if (valid)
            {
                System.out.println("VALID\n");
            }
            else
            {
                System.out.println("INVALID\n");
            }

            if (valid)
            {
                Utilities.writeFile(symDecryptedBytes, "DecryptedFile.txt");
                System.out.println("Sending Client acknowledgement that file was recieved successfully and validated\n");
                out.writeBoolean(true);
            }
            else
            {
                System.out.println("\nSending Client acknowledgement that file was not validated");
                System.out.println("The recieved file is not the same as when it was sent. Discarding the file. Please try Again\n");
                out.writeBoolean(false);
                System.exit(-1);
            }

            recSocket.close();
            servSocket.close();
            in.close();
            out.close();
            
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
