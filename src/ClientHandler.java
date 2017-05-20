/**
 * Created by eduardorobles on 5/13/17.
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private PrintWriter writeToClient;
    private BufferedReader inputFromtClient;
    private Server mainServer;
    String host = "localhost";
    int port = 4444;

    public ClientHandler(Server mainServer,Socket clientSocket)
    {
        this.mainServer = mainServer;
        this.clientSocket = clientSocket;
    }

    private void setup_streams()
    {
        try
        {
            //create output stream , be able to write to client
            writeToClient = new PrintWriter(clientSocket.getOutputStream());

            //create input stream, be able to receive from client
            inputFromtClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        catch(IOException e )
        {
            e.printStackTrace();
            System.out.println("Error setting up I/O streams with client");
        }

    }

    public void run()
    {
        setup_streams();
        handleClientConnection();
    }

    private void handleClientConnection()
    {
        //Write Welcome Message to Client
        writeToClient.println("Welcome to the Ping Chat");
        writeToClient.flush();

        String line;

        try
        {
            while ((line = inputFromtClient.readLine()) != null)
            {


                /*
                ArrayList<ClientHandler> clients = mainServer.getClientHandlers();
                for(ClientHandler client: clients)
                {
                    if(client != this)
                    {
                        client.sendMessageToclient(line);
                    }
                }
                */
            }
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }

        System.out.println("Client has succesfully Disconnected");

    }

    private void handle_login(String [] data)
    {
        if(mainServer.userAccounts.findAccount(data[1],data[2]))
        {
            User newUser = new User();
            mainServer.userAccounts.copyAccount(data[1],newUser);
            writeToClient.println("true");
            ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
            chat.run();
        }
        else
        {
            writeToClient.println("false");
        }
    }

    private void sendMessageToclient(String message)
    {
        writeToClient.println(message);
        writeToClient.flush();
    }
}
