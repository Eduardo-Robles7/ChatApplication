import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by eduardorobles on 5/12/17.
 */
public class Server
{
    //port number and list of Client Handlers
    private int port;
    private ServerSocket serverSocket;
    //private Socket clientSocket;
    private ArrayList<ClientHandler>ClientHandlers;
    AccountManager userAccounts;

    public Server(int port)
    {
        this.port = port;
        this.ClientHandlers = new ArrayList<>();
        this.userAccounts = new AccountManager();
    }

    public ArrayList<ClientHandler> getClientHandlers()
    {
        return ClientHandlers;
    }

    public void removeClientHandler(ClientHandler client)
    {
        ClientHandlers.remove(client);
    }

    public void run()
    {
        try
        {
            //create the server socket
            serverSocket = new ServerSocket(this.port);
            System.out.println("Server Started....");

            //keep looping forever to receive connections
            while(true)
            {
                System.out.println("Waiting for Client Connection....");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted from: "+clientSocket);

                //create a new thread to handle that client
                ClientHandler newClient = new ClientHandler(this,clientSocket);
                ClientHandlers.add(newClient);
                newClient.start();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String [] args)
    {
        int port = 4444;
        Server mainServer = new Server(port);
        mainServer.run();
    }
}
