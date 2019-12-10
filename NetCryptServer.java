
public class NetCryptServer {
    public static void main(String[] args) {
        if (args.length == 1)
        {
            NetCrypt net = new NetCrypt(args, false, true);
        }
        else
        {
            System.out.println("Invalid Arguements:\n\tEx. java NetCryptServer [port #]");
            System.exit(-1);
        }
    }
}