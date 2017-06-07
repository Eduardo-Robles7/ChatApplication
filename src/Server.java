import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *  Class: Server
 *  Desc: This class implements the central Server for the Chat Application.
 *        It is in charge of accepting client connections and passing on information
 */

public class Server
{
    //port number and list of Client Handlers
    private int port;
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler>ClientHandlers;
    private DefaultListModel model;
    private ArrayList<ClientChatWindow> clientChats;
    AccountManager userAccounts;


    /**
     * Server
     * This creates the central server for the Chat Application
     * @param port - The port number the server should listen on
     */
    public Server(int port)
    {
        this.port = port;
        this.ClientHandlers = new ArrayList<>();
        this.userAccounts = new AccountManager();
        this.model = new DefaultListModel();
        this.clientChats = new ArrayList<>();
    }

    /**
     * getClientHandlers
     * This method returns the list of the client handlers
     * @return ArrayList<ClientHandler> - The list of Clients connected to the Server
     */
    public ArrayList<ClientHandler> getClientHandlers()
    {
        return ClientHandlers;
    }

    /**
     * removeClientHandler
     * This method takes a client as an argument, and removes it from the list of client handlers
     * @param client
     */
    public void removeClientHandler(ClientHandler client)
    {
        ClientHandlers.remove(client);
    }

    /**
     * getOnlineUsersList
     * This method returns a model object which contains the list of online users
     * @return model - the model that stores the list of online users.
     */
    public DefaultListModel getOnlineUsersList()
    {
        return this.model;
    }

    /**
     * addOnlineUserToModel
     * This method adds a user to the list of online users
     * @param user_name - the username to add
     */
    public void addOnlineUserToModel(String user_name)
    {
       this.model.addElement(user_name);
    }

    /**
     * removeOnlineUserFromModel
     * This method removes a user from the model of online users
     * @param user_name - the user to remove from the model
     */
    public void removeOnlineUserFromModel(String user_name)
    {
       this.model.removeElement(user_name);
    }

    /**
     * getClientChats
     * This method returns the list of all the Client Chat Windows
     * @return  ArrayList<ClientChatWindow> - The list holding all the client chat windows
     */
    public ArrayList<ClientChatWindow> getClientChats()
    {
        return clientChats;
    }

    /**
     * addClientChat
     * This method add a Client Chat Window to the list of Client Chat Windows active
     * @param client - the client chat window to add
     */
    public void addClientChat(ClientChatWindow client)
    {
        clientChats.add(client);
    }

    /**
     * removeClientChat
     * This methods removes a Client Chat Window from the list
     * @param client - the client chat window to remove
     */
    public void removeClientChat(ClientChatWindow client)
    {
        clientChats.remove(client);
    }

    /**
     * sendPrivateChatToUser
     * This method sends a message to a specific user only, a private chat
     * @param recipient - the user receiving the message
     * @param sender - the user sending the message
     * @param message - the chat message
     */
    public void sendPrivateChatToUser(String recipient,String sender,String message)
    {
        //loop through all the clients in array list
        for(ClientChatWindow client: clientChats)
        {
            if(client.getUserName().equals(recipient))
                client.sendToChatAreaPrivate(sender+":"+message);
        }
    }

    /**
     * run
     * This method starts the Server on the selected port, accepts user socket connections, creates threads
     */
    public synchronized void run()
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

    /**
     * main
     * This main method runs the Server on the port selected
     * @param args
     */
    public static void main(String [] args)
    {
        int port = 4444;

        //create a Server and run it
        Server mainServer = new Server(port);
        mainServer.run();
    }
}
