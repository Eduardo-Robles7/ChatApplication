import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by eduardorobles on 5/13/17.
 */
public class ClientChatWindow extends JFrame
{
    private String hostName;
    private int port;
    private Socket connectionToServer;
    private PrintWriter writeToServer;
    private BufferedReader inputFromServer;
    private BufferedReader inputFromKeyBoard;
    private String userName;

    //GUI Components
    private JPanel mainPanel;
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextField userInput;
    private JButton sendButton;
    private JButton exitButton;
    private JButton recordsButton;

    public ClientChatWindow(String hostName,int port)
    {
        this.hostName = hostName;
        this.port = port;
        this.userName = "Pablo";
    }

    private void create_GUI()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);

        //North Panel
        JPanel northPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanel,BorderLayout.NORTH);

        //center panel
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane);


        //south panel
        JPanel southPanel = new JPanel(new BorderLayout());
        mainPanel.add(southPanel,BorderLayout.SOUTH);
        exitButton = new JButton("Exit Chat");
        recordsButton = new JButton("Records");
        userInput = new JTextField();
        southPanel.add(userInput,BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        southPanel.add(buttonPanel,BorderLayout.EAST);
        buttonPanel.add(recordsButton,BorderLayout.WEST);
        buttonPanel.add(exitButton,BorderLayout.EAST);

        setTitle("Ping Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700,500);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

    }

    public void run()
    {
        create_GUI();
        setup_streams();
        setup_buttons();
        listenToServer();
    }

    private void setup_streams()
    {
        try
        {
            connectionToServer = new Socket(this.hostName,this.port);
            writeToServer = new PrintWriter(connectionToServer.getOutputStream());
            inputFromServer = new BufferedReader(new InputStreamReader(connectionToServer.getInputStream()));
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }
    }

    private void setup_buttons()
    {
        userInput.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //sendtoChatArea(userInput.getText());
                writeToServer.println(userInput.getText());
                //System.out.println(userInput.getText());
                writeToServer.flush();
                sendtoChatArea(userInput.getText());
            }
        });

    }

    private void listenToServer()
    {
           String line;
            try
            {
                while ((line = inputFromServer.readLine()) != null)
                {
                    System.out.println(line);
                    sendtoChatArea(line);
                }
            }
            catch(IOException e)
            {
            }
    }

    private void sendtoChatArea(String message)
    {
        if (message != null)
        {
            chatArea.append(userName+":"+message + "\n");
            //writeToServer.println(message + "\n");
            //writeToServer.flush();
            userInput.setText(" ");

        }
    }
    public static void main(String [] args)
    {
        String host = "localhost";
        int port = 4444;
        ClientChatWindow chat = new ClientChatWindow(host,port);
        chat.run();
    }
}
