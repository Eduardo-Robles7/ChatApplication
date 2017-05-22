/**
 * Created by eduardorobles on 5/13/17.
 */
import javax.imageio.IIOException;
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
    private String newUserMessage = " has connected to the chat\n";


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

        String line;

        try
        {
            while ((line = inputFromtClient.readLine()) != null)
            {

                String tokens[] = line.split(":");
                String command = tokens[0];

                if(command.equalsIgnoreCase("login"))
                {
                    handle_login(tokens);
                }

                else if(command.equalsIgnoreCase("disconnect"))
                {
                    handle_disconnect(tokens);
                }

                else if(command.equalsIgnoreCase("register"))
                {
                    handle_register(tokens);
                }

                else if(command.equalsIgnoreCase("msg"))
                {
                    handle_message(tokens);
                }
            }
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }
    }

    private void handle_login(String [] data) throws IOException
    {
        if(mainServer.userAccounts.findAccount(data[1],data[2]))
        {
            User newUser = new User();
            mainServer.userAccounts.copyAccount(data[1],newUser);
            sendMessageToclient("true");
            broadcastToAll(newUser.getUser_name()+newUserMessage);
            ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
            chat.run();
        }
        else
        {
            sendMessageToclient("false");
        }
    }

    private void handle_disconnect(String [] data)
    {
        mainServer.removeClientHandler(this);
        broadcastToAll(data[1]+" has disconnected from the chat");
    }

    private void handle_register(String [] data)
    {
        User newUser = new User(data[1],data[2]);
        mainServer.userAccounts.addNewUser(newUser);
        sendMessageToclient("true");
        broadcastToAll(newUser.getUser_name()+newUserMessage);
        ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
        chat.run();
    }

    private void broadcastToAll(String message)
    {
        ArrayList<ClientHandler> clients = mainServer.getClientHandlers();
        for(ClientHandler client: clients)
        {
                client.sendMessageToclient(message);
        }
    }

    private void handle_message(String [] data)
    {
        ArrayList<ClientHandler> clients = mainServer.getClientHandlers();
        for(ClientHandler client: clients)
        {
            if(client != this)
            {
                client.sendMessageToclient(data[1]+":"+data[2]);
            }
        }
    }

    private void sendMessageToclient(String message)
    {
        writeToClient.println(message);
        writeToClient.flush();
    }
}
