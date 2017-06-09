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
import java.util.Calendar;
import java.text.SimpleDateFormat;


/**
 *  Class: ClientChatWindow
 *  Desc: This class implements the Graphical User Interface for the User.
 *        It creates the window, chat area , list of online users, messages
 */

public class ClientChatWindow extends JFrame implements Runnable
{
    //Network components
    private String hostName;
    private int port;
    private Socket connectionToServer;
    private PrintWriter writeToServer;
    private BufferedReader inputFromServer;
    private BufferedReader inputFromKeyBoard;
    private User user;
    private Date currentDate;
    private Server mainServer;


    //GUI Components
    private JPanel mainPanel;
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextField userInput;
    private JButton sendButton;
    private JButton exitButton;
    private JButton recordsButton;
    private JButton clearButton;
    private JList onlineUsersList;
    private DefaultListModel model;
    private JSplitPane splitPane;


    /**
     * ClientChatWindow
     * Constructor, creates a Client Chat Window given a User Account
     * @param user - user account
     * @param hostName - host to connect to
     * @param port - port number to listen to
     * @param model - model that contains list of online users
     * @param mainServer - instance of the main server
     */
    public ClientChatWindow(User user,String hostName,int port,DefaultListModel model,Server mainServer)
    {
        this.hostName = hostName;
        this.port = port;
        this.user = new User(user);
        this.currentDate = new Date();
        onlineUsersList = new JList();
        this.model = new DefaultListModel();
        this.model = model;
        onlineUsersList.setModel(this.model);
        this.mainServer = mainServer;
        this.mainServer.addClientChat(this);
    }

    /**
     * create_GUI
     * This method creates the Graphical User Interface for the User to Chat with.
     * Messages are displayed and entered.
     * Buttons for accessing chat records
     */
    private void create_GUI()
    {
        //create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);

        //North Panel
        JPanel northPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanel,BorderLayout.NORTH);

        //Create the chat area and online users panel
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(scrollPane);
        JScrollPane sp = new JScrollPane(onlineUsersList);
        splitPane.setRightComponent(sp);
        splitPane.setDividerLocation(520);
        splitPane.setEnabled(false);
        mainPanel.add(splitPane);

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

        //set title and window properties
        setTitle("Ping Chat");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);   //This was added after
        setSize(680,370);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    /**
     * run
     * This methods create the GUI, sets up socket connections, listens for input from the server
     * This is the controller that starts everything
     */
    public void run()
    {
        create_GUI(); //create gui
        setup_streams();//setup streams
        setup_buttons();//setup button actions
        setup_disconnect();//setup disconnect properties
        listenToServer(); //listen to server for input

        //remove self from list after disconnecting
        mainServer.removeClientChat(this);
    }

    /**
     * setup_streams
     * This method establishes input and output streams with the Server
     */
    private void setup_streams()
    {
        try
        {
            //copy socket information over
            connectionToServer = new Socket(this.hostName,this.port);

            //create outputstream to write to the Server
            writeToServer = new PrintWriter(connectionToServer.getOutputStream());

            //create inputstream to receive from the Server
            inputFromServer = new BufferedReader(new InputStreamReader(connectionToServer.getInputStream()));
        }

        catch(IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * setup_buttons
     * This method sets all the actions for the Buttons on the GUI.
     * send button, clear button, show records button,private chat
     */
    private void setup_buttons()
    {
        //-------User Enters and Sends Message---------//
        userInput.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //If empty input, don't send message
                if(userInput.getText().isEmpty())
                    return;

                String message = user.getUser_name()+":"+userInput.getText();
                sendtoChatArea(message);
                user.writeToChatLog(message+"\n"+"("+getTime()+")"+"\n");

                //send public message
                if(onlineUsersList.isSelectionEmpty() || onlineUsersList.getSelectedValue().toString().equalsIgnoreCase(user.getUser_name()))
                {
                    sendMessageToServer("msg:"+message);
                    onlineUsersList.clearSelection();
                }

                //send private message
                else
                {
                    String recipient = onlineUsersList.getSelectedValue().toString();
                    sendMessageToServer("private"+":"+recipient+":"+message);
                    onlineUsersList.clearSelection();
                }
             }
        });


        //--------Clear Button-----------------------//
        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                userInput.setText("");
            }
        });

        //--------Display Chat Records Button---------------//
        recordsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
               //model.addElement("Pablo");
                user.display_chatlog();
            }
        });
    }

    /**
     * setup_disconnect
     * This method is called when a User disconnects from the Chat Window.
     * The Server is notified which user and the connections to server are closed.
     */
    private void setup_disconnect()
    {
        //before disconnecting, send username to Server
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

    /**
     * listenToServer
     * This method waits for Input from the Server and calls corresponding function
     * It handles messages,login, disconnects
     */
    private void listenToServer()
    {
        welcomeMessage();
           String line;
            try
            {
                //read input from the Server
                while ((line = inputFromServer.readLine()) != null)
                {
                    //get the type of command
                    String data [] = line.split(":");
                    String cmd = data[0];

                    //normal message
                    if(cmd.equalsIgnoreCase("msg"))
                    {
                        handle_message(data);
                    }

                    //login message
                    else if(cmd.equalsIgnoreCase("login"))
                    {
                        handle_login(data);
                    }

                    //disconnect message
                    else if(cmd.equalsIgnoreCase("disconnect"))
                    {
                        handle_disconnect(data);
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
    }

    /**
     * handle_message
     * This method takes the message from the Server and displays it to the chat area
     * @param data - the message from the Server
     */
    private void handle_message(String [] data)
    {
        //write to Chat Log
        String message = data[1]+":"+data[2];
        user.writeToChatLog(message+"\n"+"("+getTime()+")"+"\n");

        //write to the chat area
        sendtoChatArea(message);
    }

    /**
     * handle_login
     * This method displays a new user that has entered the chat
     * @param data - the username that has logged in
     */
    private void handle_login(String [] data)
    {
       //write new user to the chat area
       sendtoChatArea(data[1]+" has connected to the chat");
    }

    /**
     * handle_disconnect
     * This method notifies when a user has logged of from the chat
     * @param data - the username that has logged off
     */
    private void handle_disconnect(String [] data)
    {
        //write that a user has disconnected from the chat
        sendtoChatArea(data[1]+ " has disconnected from the chat");
    }

    /**
     * sendToChatArea
     * This methods takes a message and writes it to the chat area
     * @param message - message
     */
    private void sendtoChatArea(String message)
    {
        //if message is not empty, write to the chat area
        if (message != null)
        {
            chatArea.append(message+"\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    /**
     * sendToChatAreaPrivate
     * This method takes and writes it to the chat area, used for private messages
     * @param message - message
     */
    public void sendToChatAreaPrivate(String message)
    {
        //if message is not empty, write the private message to the chat area
        if (message != null)
        {
            user.writeToChatLog(message+"\n"+"("+getTime()+")"+"\n");
            chatArea.append(message+"\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    /**
     * sendMessageToServer
     * This methods takes a message and sends it to the Server input stream
     * @param message - message
     */
    private void sendMessageToServer(String message)
    {
        //sends message to Server
        writeToServer.println(message);
        writeToServer.flush();
        userInput.setText("");
    }

    /**
     * welcomeMessage
     * This method sends a welcome message when the user logs on
     */
    private void welcomeMessage()
    {
       sendtoChatArea("Welcome To Ping Chat \n"+currentDate.toString());
       sendtoChatArea(user.getUser_name()+" connected to Chat\n");
    }

    /**
     * getUserName
     * This method returns the username of the User using the chat
     * @return - username of chat user
     */
    public String getUserName()
    {
        return user.getUser_name();
    }

    /**
     * getTime
     * This methods returns a time stamp of the current time
     * @return - current time represented in string format
     */
    private String getTime()
    {
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    public static void main(String [] args)
    {
        String host = "localhost";
        int port = 4444;
        /*
        User newUser = new User("Asds","Asdas");
        //ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
        //chat.run();
        */
    }
}
