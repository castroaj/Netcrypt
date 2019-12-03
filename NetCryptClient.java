import java.security.spec.EncodedKeySpec;

public class NetCryptClient {
    public static void main(String[] args) {
        if (args.length > 0)
        {
            NetCryptCmd net = new NetCryptCmd(args, true, false);
        }
        else
        {
            System.err.print("Invalid number of arguments");
            
        }
    }
}