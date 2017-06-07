import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

class ChatLog
{
    private String file_name;
    private JFrame frame;
    private JPanel mainPanel;
    private JTextArea chatArea;

    /**
     * ChatLog
     * Default constructor, sets filename to null
     */
    public ChatLog()
    {
        this.file_name = null;
    }

    /**
     * ChatLog
     * ChatLog constructor, creates a new chat log record
     * @param file_name - the name of the chat log file (text file)
     */
    public ChatLog(String file_name)
    {
        this.file_name = file_name;
    }

    /**
     * buildGui
     * This method builds the graphical user interface for the chat records window
     * It consists of a window and a scrollbar
     */
    private void buildGUI()
    {
        frame = new JFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.getContentPane().add(mainPanel);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane);
        frame.setTitle("Chat Records");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700,500);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * displayChatLog
     * This method creates a window and displays a users chat history
     * @throws FileNotFoundException
     */
    public void displayChatLog() throws FileNotFoundException
    {
        buildGUI();
        displayChatLog(file_name);
    }

    /**
     * displayChatLog
     * This method reads chat records from a text file and outputs it to the window
     * @param file_name - the file containing the chat records
     * @throws FileNotFoundException
     */
    private  void displayChatLog(String file_name) throws FileNotFoundException
    {
        File file = new File(file_name);
        Scanner fsc = new Scanner(file);
        while(fsc.hasNextLine())
        {
            String line = fsc.nextLine();
            chatArea.append(line+"\n");
        }
        fsc.close();
    }

    /**
     * writeToChatLog
     * This method writes user messages to the text file
     * @param message - the message to write
     */
    public void writeToChatLog(String message)
    {
        File file = new File(file_name);
        try
        {
            FileWriter fileWriter = new FileWriter(file,true);
            BufferedWriter buffer = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(buffer);

            if(!file.exists())
            {
                //create a new file
                file.createNewFile();
            }

            printWriter.println(message);
            printWriter.close();
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

public class User
{
    private String user_name;
    private String password;
    private ChatLog chatLog;

    /**
     * User
     * This is the default constructor
     * Initializes user info to null state
     */
    public User()
    {
        this.user_name = null;
        this.password = null;
        this.chatLog = null;
    }

    /**
     * User
     * Constructor, initializes username and password
     * @param user_name - username
     * @param password - password
     */
    public User(String user_name, String password)
    {
        this.user_name = user_name;
        this.password  = password;
        this.chatLog = new ChatLog(user_name+".txt");
    }

    /**
     * User
     * Copy Constructor
     * @param otherUser - user account to copy from
     */
    public User(User otherUser)
    {
        this.user_name = otherUser.user_name;
        this.password = otherUser.password;
        this.chatLog = new ChatLog("records/"+user_name+".txt");
    }

    /**
     * Copy
     * This method copies the information from another User object
     * @param otherUser - the user account to copy from
     */
    public void copy(User otherUser)
    {
        this.user_name = otherUser.user_name;
        this.password = otherUser.password;
    }

    /**
     * getUser_name
     * This methods returns username
     * @return - username
     */
    public String getUser_name()
    {
        return user_name;
    }

    /**
     * setUser_name
     * This method sets a new username
     * @param user_name - the new username for the account
     */
    public void setUser_name(String user_name)
    {
        this.user_name = user_name;
    }

    /**
     * getPassword
     * This method returns the user's password
     * @return - password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * setPassword
     * This method sets a user's password
     * @param password - new password for account
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * display_chatlog
     * This method displays a user's chat record
     */
    public void display_chatlog()
    {
        try
        {
            chatLog.displayChatLog();
        }
        catch(FileNotFoundException e)
        {
           e.printStackTrace();
        }
    }

    /**
     * writeToChatLog
     * This method lets the user write to the chat log
     * @param message - the message to write
     */
    public void writeToChatLog(String message)
    {
      chatLog.writeToChatLog(message);
    }

    /**
     * display
     * This methods displays a User's name and password
     */
    public void display()
    {
        System.out.println(user_name+"|"+password);
    }

}

class AccountManager
{
    private HashMap<String,User> map;
    private final int MAX_SIZE = 10;
    String file = "Users.txt";

    /**
     * AccountManager
     * Default Constructor, loads accounts into HashMap from a file
     */
    public AccountManager()
    {
        map = new HashMap<>();
        try
        {
            loadFromFile(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("Error Finding File");
        }
    }

    /**
     * AccountManager
     * Constructor, loads user accounts from a file
     * @param file_name - text file containing user accounts
     */
    public AccountManager(String file_name)
    {
        map = new HashMap<>();
        try
        {
            loadFromFile(file_name);
        }
        catch(FileNotFoundException e )
        {
            e.printStackTrace();
            System.out.println("Error Finding File");
        }

    }

    /**
     * add
     * This methods add a new user to the Account database
     * @param newUser - the new user to add
     */
    private void add(User newUser)
    {
        map.put(newUser.getUser_name(),newUser);
    }

    /**
     * addNewUser
     * This method creates a new user and adds it to the Account database
     * @param user_name - username
     * @param password - password
     */
    public void addNewUser(String user_name,String password)
    {
        //create new user
        User newUser = new User(user_name,password);

        //add to the hash map
        map.put(newUser.getUser_name(),newUser);

        //add to the text file
        addUserToFile(newUser,file);
    }

    /**
     * addNewUser
     * This methods adds a new user to the Account database
     * @param newUser - new user account
     */
    public void addNewUser(User newUser)
    {
        //add user to hash map
        map.put(newUser.getUser_name(),newUser);

        //add user to the text file
        addUserToFile(newUser,file);
    }

    /**
     * findAccount
     * @param user_name - account username
     * @param password - account password
     * @return - true if account is found, false otherwise
     */
    public boolean findAccount(String user_name,String password)
    {
        //if hash map contains matching username
        if(map.containsKey(user_name))
        {
            //check for matching credentials
            if(password.equals(map.get(user_name).getPassword()))
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * copyAccount
     * This method looks for a matching account, if found it copies the information over
     * @param user_name - account username
     * @param newUser - account password
     * @return - true if account was copied, false if no matching account is found
     */
    public boolean copyAccount(String user_name,User newUser)
    {
        //if hash map has that username
        if(map.containsKey(user_name))
        {
            //copy the account details
            newUser.setUser_name(map.get(user_name).getUser_name());
            newUser.setPassword(map.get(user_name).getPassword());
            return true;
        }
        return false;
    }

    /**
     * loadFromFile
     * This methods reads from a file and loads all the accounts into the database
     * @param file_name - text file containing accounts
     * @throws FileNotFoundException
     */
    private void loadFromFile(String file_name)throws FileNotFoundException
    {
        File file = new File(file_name);
        Scanner fsc = new Scanner(file);
        while(fsc.hasNextLine())
        {
            String line = fsc.nextLine();
            String tokens[] = line.split("\\|");
            User newUser = new User(tokens[0],tokens[1]);
            add(newUser);
        }
        fsc.close();
    }

    /**
     * addUserToFile
     * This method adds a new user to the text file
     * @param newUser - new User
     * @param file_name - filename of users
     */
    private void addUserToFile(User newUser,String file_name)
    {
        File file = new File(file_name);
        try
        {
            FileWriter fileWriter = new FileWriter(file,true);
            BufferedWriter buffer = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(buffer);

            if(!file.exists())
            {
                //create a new file
                file.createNewFile();
            }

            String line = newUser.getUser_name()+"|"+newUser.getPassword();
            printWriter.println(line);
            printWriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * display_users
     * This method displays all the user accounts to the command line
     */
    public void display_users()
    {
        //display all the users in the hash map
        for(String user_name : map.keySet())
        {
            System.out.println(user_name);
        }
    }

    /**
     * main
     * This method test Account and new User functionality
     * @param args
     */
    public static void main(String [] args)
    {
        AccountManager Mananger = new AccountManager();
        User newUser = new User();
        Mananger.copyAccount("Robles5",newUser);
        newUser.display();
        Mananger.display_users();
    }
}
