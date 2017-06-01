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


    public ClientChatWindow(User user,String hostName,int port,DefaultListModel model,Server mainServer)
    {
        this.hostName = hostName;
        this.port = port;
        this.user = new User(user);
        //this.user.copy(user);       ///Original Before adding ChatLog
        this.currentDate = new Date();
        onlineUsersList = new JList();
        this.model = new DefaultListModel();
        this.model = model;
        onlineUsersList.setModel(this.model);
        this.mainServer = mainServer;
        this.mainServer.addClientChat(this);
    }

    private void create_GUI()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);

        //Online Users Panel

        //onlineUsersList = new JList();
        //model = new DefaultListModel();
        //onlineUsersList.setModel(model);
        //model.addElement("GuestUser1");

        //North Panel
        JPanel northPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanel,BorderLayout.NORTH);

        //center panel FOR NOW ///////////////////////////////
        /*
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane);
        */
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
       // buttonPanel.add(exitButton,BorderLayout.EAST);

        setTitle("Ping Chat");
        //setDefaultCloseOperation(EXIT_ON_CLOSE);    ////KEEEP THIS
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
        mainServer.removeClientChat(this);
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
                //String timeStamp = new SimpleDateFormat("yyyy-MMdd-HHmmss").format(Calendar.getInstance().getTime());
                String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                user.writeToChatLog(message+"\n"+"("+timeStamp+")"+"\n");
                if(onlineUsersList.isSelectionEmpty())
                {
                    sendMessageToServer("msg:"+message);
                }
                else
                {
                    String recipient = onlineUsersList.getSelectedValue().toString();
                    sendMessageToServer("private"+":"+recipient+":"+message);
                    onlineUsersList.clearSelection();
                }
             }
        });

        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                userInput.setText("");
            }
        });

        recordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               //model.addElement("Pablo");
                user.display_chatlog();
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
                    String data [] = line.split(":");
                    String cmd = data[0];

                    if(cmd.equalsIgnoreCase("msg"))
                    {
                        handle_message(data);
                    }

                    else if(cmd.equalsIgnoreCase("login"))
                    {
                        handle_login(data);
                    }

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

    private void handle_message(String [] data)
    {
        sendtoChatArea(data[1]+":"+data[2]);
    }

    private void handle_login(String [] data)
    {
       sendtoChatArea(data[1]+" has connected to the chat");
       //model.addElement(data[1]);
    }

    private void handle_disconnect(String [] data)
    {
        sendtoChatArea(data[1]+ " has disconnected from the chat");
        //model.removeElement(data[1]);
    }

    private void sendtoChatArea(String message)
    {
        if (message != null)
        {
            chatArea.append(message+"\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    public void sendToChatAreaPrivate(String message)
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

    public String getUserName()
    {
        return user.getUser_name();
    }

    public static void main(String [] args)
    {
        String host = "localhost";
        int port = 4444;
        User newUser = new User("Asds","Asdas");
        //ClientChatWindow chat = new ClientChatWindow(newUser,host,port);
        //chat.run();
    }
}
