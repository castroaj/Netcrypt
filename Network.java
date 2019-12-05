import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class Network {
    public static Socket createSocket(String ip, int port, String user)
    {
        Socket socket = null;
        try 
        {
            socket = new Socket(ip, port);
            System.out.println("Connected to "+ user +" Socket at IP " + ip + ", and Port #: " + port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        return socket;
    }


    public static ServerSocket createServerSocket(int port, String user)
    {
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started at Port #: " + port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        return server;
    }
}