import java.io.File;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class NetCryptCmd {

    public NetCryptCmd(String[] args) {
        start(args);
    }

    public void start(String[] args)
    {
        // Index0 = valid, Index1 = networkRequest
        boolean[] parsedArgs = new boolean[2];
        String fileName = args[0];
        Cipher cipher;
        SecureRandom r;
        SecretKey s_key;
        IvParameterSpec IV;

        parsedArgs = parseArgs(args);

        if (parsedArgs[0])
        {
            try 
            {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                r = new SecureRandom();
                s_key = CryptMachine.generateKey(128, r);
                IV = CryptMachine.generateIV(r);

                System.out.println();
                System.out.println("N E T C R Y P T    S T A R T E D:");
                System.out.println("=================================");

                File encryptedFile = CryptMachine.encryptFile(fileName, cipher, r, s_key, IV);

                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                File decryptedFile = CryptMachine.decryptFile(encryptedFile.getPath(), cipher, s_key, IV);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            
        }
        else
        {
            System.err.print("Invalid Arguements");
        }


    }

    private boolean[] parseArgs(String[] args)
    {
        boolean[] argParse = new boolean[2];

        if (args.length == 1)
        {
            argParse[0] = true;
            argParse[1] = false;
        }
        else if (args.length == 3)
        {
            argParse[0] = true;
            argParse[1] = true;
        }
        else
        {   
            argParse[0] = false;
            argParse[1] = false;
        }
        return argParse;
    }
}