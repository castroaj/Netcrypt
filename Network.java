import java.net.*;

public class Network {
    public static Socket createSocket(String ip, int port, String user)
    {
        Socket clientSocket = null;
        try 
        {
            System.out.println("Will try to connected to "+ user +" Socket at IP " + ip + ", and Port #: " + port + "\n");
            clientSocket = new Socket(ip, port);
            System.out.println("Connected to "+ user +" Socket at IP " + ip + ", and Port #: " + port);
        }
        catch (Exception e)
        {
            System.out.println("Connection to NetCrypt server could not be established at the following:\nIP: " + ip + "\nPort: " + port+ "\n\n");
            System.exit(-1);
        }
        return clientSocket;
    }


    public static ServerSocket createServerSocket(int port, String user)
    {
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started listening at Port #: " + port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        return server;
    }
}