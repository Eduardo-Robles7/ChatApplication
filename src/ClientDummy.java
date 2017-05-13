import java.io.IOException;
import java.net.Socket;

/**
 * Created by eduardorobles on 5/13/17.
 */
public class ClientDummy
{
    private String hostName;
    private int port;
    private Socket connectionToServerSocket;

    public ClientDummy(String host, int port)
    {
        this.hostName = host;
        this.port= port;
    }

    public void run()
    {
        try
        {
            this.connectionToServerSocket = new Socket(this.hostName,this.port);
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String args [])
    {
        String host = "localhost";
        int port = 4444;

        ClientDummy Client = new ClientDummy(host,port);
        Client.run();
    }
}
