
public class NetCryptServer {
    public static void main(String[] args) {
        if (args.length == 0)
        {
            NetCryptCmd net = new NetCryptCmd(args, false, true);
        }
        else
        {
            System.err.print("Invalid number of arguments");
            
        }
    }
}