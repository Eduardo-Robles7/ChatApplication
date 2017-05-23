import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by eduardorobles on 5/13/17.
 */
public class ClientChatWindow extends JFrame implements Runnable
{
    private String hostName;
    private int port;
    private Socket connectionToServer;
    private PrintWriter writeToServer;
    private BufferedReader inputFromServer;
    private BufferedReader inputFromKeyBoard;
    private User user;
    private Date currentDate;


    //GUI Components
    private JPanel mainPanel;
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextField userInput;
    private JButton sendButton;
    private JButton exitButton;
    private JButton recordsButton;
    private JButton clearButton;
    

    public ClientChatWindow(User user,String hostName,int port)
    {
        this.hostName = hostName;
        this.port = port;
        this.user = new User();
        this.user.copy(user);
        this.currentDate = new Date();
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
        clearButton = new JButton("Clear");
        buttonPanel.add(clearButton,BorderLayout.WEST);
        buttonPanel.add(recordsButton,BorderLayout.EAST);  //Had Center before changing
       // buttonPanel.add(exitButton,BorderLayout.EAST);

        setTitle("Ping Chat");
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    ////KEEEP THIS
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);   //This was added after
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
        setup_disconnect();
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
            public void actionPerformed(ActionEvent e)
            {
                String message = user.getUser_name()+":"+userInput.getText();
                sendtoChatArea(message);
                sendMessageToServer("msg:"+message);
            }
        });

        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                userInput.setText("");
            }
        });
    }

    private void setup_disconnect()
    {
        super.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                    sendMessageToServer("disconnect:"+user.getUser_name());
                    dispose();
            }
        });
    }

    private void listenToServer()
    {
        welcomeMessage();
           String line;
            try
            {
                while ((line = inputFromServer.readLine()) != null)
                {
                    sendtoChatArea(line);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
    }


    private void sendtoChatArea(String message)
    {
        if (message != null)
        {
            chatArea.append(message+"\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private void sendMessageToServer(String message)
    {
        writeToServer.println(message);
        writeToServer.flush();
        userInput.setText("");
    }

    private void welcomeMessage()
    {
       sendtoChatArea("Welcome To Ping Chat \n"+currentDate.toString());
       sendtoChatArea(user.getUser_name()+" connected to Chat\n");
    }



    public static void main(String [] args)
    {
        String host = "localhost";
        int port = 4444;
        User newUser = new User("Asds","Asdas");
        ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
        chat.run();
    }
}
