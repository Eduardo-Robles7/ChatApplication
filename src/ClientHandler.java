/**
 * Created by eduardorobles on 5/13/17.
 */
import javax.imageio.IIOException;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *  Class: ClientHandler
 *  Desc: This class takes a socket connection from the main server and handles all communication with the client.
 */
public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private PrintWriter writeToClient;
    private BufferedReader inputFromtClient;
    private Server mainServer;
    private  String host = "localhost";
    private int port = 4444;

    /**
     * ClientHandler
     * Constructor, takes an instance of the main Server and the Client socket that connected
     * @param mainServer - main server
     * @param clientSocket - client socket that connected to the main server
     */
    public ClientHandler(Server mainServer,Socket clientSocket)
    {
        this.mainServer = mainServer;
        this.clientSocket = clientSocket;
    }

    /**
     * setup_streams
     * This method setups the input and output streams for the client socket
     */
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

    /**
     * run
     * This methods setups the Client socket streams and starts listening for client interaction
     */
    public void run()
    {
        setup_streams();
        handleClientConnection();
    }

    /**
     * handleClientConnections
     * This methods listens for input from the client
     * It servers as a controller and passes on the information to the corresponding function for that task
     */
    private void handleClientConnection()
    {

        String line;

        try
        {
            //wait for input
            while ((line = inputFromtClient.readLine()) != null)
            {

                //get the command
                String tokens[] = line.split(":");
                String command = tokens[0];

                if(command.equalsIgnoreCase("login"))
                {
                    //validate login
                    handle_login(tokens);
                }

                else if(command.equalsIgnoreCase("disconnect"))
                {
                    //disconnect user
                    handle_disconnect(tokens);
                }

                else if(command.equalsIgnoreCase("register"))
                {
                    //register user
                    handle_register(tokens);
                }

                else if(command.equalsIgnoreCase("msg"))
                {
                    //send messages to users
                    handle_message(tokens);
                }

                else if(command.equalsIgnoreCase("private"))
                {
                    //send private message to user
                    handle_private_message(tokens);
                }
            }
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * handle_login
     * This method takes login info and validates it, if valid user is granted access to chat , otherwise invalid login
     * @param data - user account info
     * @throws IOException
     */
    private void handle_login(String [] data) throws IOException
    {
        //check if account exist in the database
        if(mainServer.userAccounts.findAccount(data[1],data[2]))
        {
            //if match is found copy account details and notify login client
            User newUser = new User();
            mainServer.userAccounts.copyAccount(data[1],newUser);
            sendMessageToclient("true");

            //broadcast the new user to everyone online
            broadcastToAll(data[0]+":"+newUser.getUser_name());
            mainServer.addOnlineUserToModel(newUser.getUser_name());

            //open Client Chat window with that account
            ClientChatWindow chat = new ClientChatWindow(newUser,host,port,mainServer.getOnlineUsersList(),mainServer);
            chat.run();
        }
        else
        {
            //no matching account was found
            sendMessageToclient("false");
        }
    }

    /**
     * handle_disconnect
     * This method handles the event of a user disconnecting from the chat
     * @param data - the user disconnecting
     */
    private void handle_disconnect(String [] data)
    {
        //remove from list of Client Handlers
        mainServer.removeClientHandler(this);

        //remove from list of online users
        mainServer.removeOnlineUserFromModel(data[1]);

        //notify all users someone has left
        broadcastToAll(data[0]+":"+data[1]);
    }

    /**
     * handle_register
     * This method handles the event of a user registering for the first time
     * @param data - user account info
     */
    private void handle_register(String [] data)
    {
        //create new user and add it to the database
        User newUser = new User(data[1],data[2]);
        mainServer.userAccounts.addNewUser(newUser);
        sendMessageToclient("true");

        //broadcast the new user to all users
        broadcastToAll("login"+":"+newUser.getUser_name());
        mainServer.addOnlineUserToModel(newUser.getUser_name());

        //open Client Chat window with that account
        ClientChatWindow chat = new ClientChatWindow(newUser,host,port,mainServer.getOnlineUsersList(),mainServer);
        chat.run();
    }

    /**
     * broadcastToAll
     * This methods takes a message and sends it to all the online users
     * @param message - the message
     */
    private void broadcastToAll(String message)
    {
        ArrayList<ClientHandler> clients = mainServer.getClientHandlers();
        for(ClientHandler client: clients)
        {
                client.sendMessageToclient(message);
        }
    }

    /**
     * handle_messages
     * This methods broadcasts a message to everyone except to the user sending the message
     * @param data - the message
     */
    private void handle_message(String [] data)
    {
        ArrayList<ClientHandler> clients = mainServer.getClientHandlers();
        for(ClientHandler client: clients)
        {
            if(client != this)
            {
                //send message to client
                client.sendMessageToclient(data[0]+":"+data[1]+":"+data[2]);
            }
        }
    }

    /**
     * handle_private_message
     * This methods handles the event of a private message being sent
     * @param data - receiver,sender,message
     */
    private void handle_private_message(String [] data)
    {
        //send private message
        mainServer.sendPrivateChatToUser(data[1],data[2],data[3]);
    }


    /**
     * sendMessageToClient
     * This methods takes a message and writes it to the client inputStream
     * @param message - the message
     */
    private void sendMessageToclient(String message)
    {
        writeToClient.println(message);
        writeToClient.flush();
    }
}
