import java.io.File;

public class NetCryptCmd {

    public NetCryptCmd(String[] args) {
        start(args);
    }

    public void start(String[] args)
    {
        // Index0 = valid, Index1 = networkRequest
        boolean[] parsedArgs = new boolean[2];

        String fileName = args[0];

        parsedArgs = parseArgs(args);

        if (parsedArgs[0])
        {
            CryptMachine cryptMachine = new CryptMachine();
            File encryptedFile = cryptMachine.encryptFile(fileName);
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