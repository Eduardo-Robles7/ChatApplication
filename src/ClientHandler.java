/**
 * Created by eduardorobles on 5/13/17.
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private PrintWriter writeToClient;
    private BufferedReader inputFromtClient;
    private Server mainServer;

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
        writeToClient.println("Welcome to the Chat");
        writeToClient.flush();

        String line;

        try
        {
            while ((line = inputFromtClient.readLine()) != null)
            {
                writeToClient.println(line);
                writeToClient.flush();
            }
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }

    }

    private void listenToClient()
    {
    }
}
