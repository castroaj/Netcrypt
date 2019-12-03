import java.security.spec.EncodedKeySpec;

public class Driver {
    public static void main(String[] args) {
        if (args.length > 0)
        {
            NetCryptCmd net = new NetCryptCmd(args);
        }
        else
        {
            System.err.print("Invalid number of arguments");
        }
    }
}