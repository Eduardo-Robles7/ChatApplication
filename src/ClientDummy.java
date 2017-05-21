import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by eduardorobles on 5/13/17.
 */
public class ClientDummy
{
    private String hostName;
    private int port;
    private Socket connectionToServerSocket;

    private Socket connection;
    private PrintWriter writeToServer;
    private BufferedReader inputFromServer;
    private BufferedReader inputFromKeyBoard;

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
            this.writeToServer = new PrintWriter(connectionToServerSocket.getOutputStream());
            inputFromServer = new BufferedReader(new InputStreamReader(this.connectionToServerSocket.getInputStream()));
            inputFromKeyBoard = new BufferedReader(new InputStreamReader(System.in));

            while(true)
            {
                String messageFromServer;

                try
                {
                    while((messageFromServer = inputFromServer.readLine()) != null)
                    {
                        System.out.println("Server: "+messageFromServer);


                        writeToServer.println(inputFromKeyBoard.readLine());
                        writeToServer.flush();
                    }
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
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

        //ClientDummy Client = new ClientDummy(host,port);
        //Client.run();
        User a = new User("ZlatanFatboy","asd");
        ClientChatWindow chat = new ClientChatWindow(a,host,port);
        chat.run();
    }
}
