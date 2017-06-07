import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
/**
 *  Class: LoginClient
 *  Desc: This class implements the Login feature for the Chat Application.
 *        Login screen where users can enter their username and password.
 */
public class LoginClient extends JFrame
{
    //Networking Components
    private String hostName;
    private int portNumber;
    private Socket connectionToServer;
    private PrintWriter writeToServer;
    private BufferedReader inputFromServer;
    private boolean isRunning = true;

    //GUI Components
    private JLabel userNameLabel;
    private JLabel passwdLabel;
    private JTextField userNameInput;
    private JPasswordField passwdInput;
    private JButton buttonLogin;
    private JButton buttonRegister;
    private JPanel mainPanel;

    /**
     * LoginClient
     * Constructor for LoginClient
     * @param hostName - host to connect to
     * @param portNumber - port number to connect to
     */
    public LoginClient(String hostName, int portNumber)
    {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.isRunning = true;
    }

    /**
     * run
     * This method calls all corresponding functions to build login client components
     */
    public void run()
    {
        createGUI();
        setup_streams();
        setup_buttons();
        listenToServer();
    }

    /**
     * setup_streams
     * This method creates the input and output streams to the Server
     */
    private void setup_streams()
    {
        try
        {
            //make connection to server
            connectionToServer = new Socket(hostName, portNumber);

            //be able to write to server output stream
            writeToServer = new PrintWriter(connectionToServer.getOutputStream());

            inputFromServer = new BufferedReader(new InputStreamReader(connectionToServer.getInputStream()));
        }
        catch (ConnectException e)
        {
            System.out.println("Connection to Host Failed");
            e.printStackTrace();
        } catch(IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * createGUI
     * This methods creates the Graphical User Interface of the Login Client
     * It consist of two input fields, login button, register button
     */
    private void createGUI()
    {
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        //Add user input field
        JPanel topPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel,BorderLayout.NORTH);
        userNameLabel = new JLabel("Username");
        topPanel.add(userNameLabel, BorderLayout.WEST);
        userNameInput = new JTextField();
        topPanel.add(userNameInput);

        //center panel
        passwdLabel = new JLabel("Password");
        JPanel centerPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel,BorderLayout.CENTER);
        centerPanel.add(passwdLabel,BorderLayout.WEST);
        passwdInput = new JPasswordField();
        centerPanel.add(passwdInput);

        //South Panel
        JPanel SouthPanel = new JPanel(new BorderLayout());
        mainPanel.add(SouthPanel,BorderLayout.SOUTH);
        buttonLogin = new JButton("Login");
        SouthPanel.add(buttonLogin,BorderLayout.WEST);
        buttonRegister = new JButton("Register");
        SouthPanel.add(buttonRegister,BorderLayout.EAST);

        //Make window exit application on close
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set display size
        setSize(300,100);

        //center the frame to middle of screen
        setLocationRelativeTo(null);
        setTitle("Ping Chat Login");
        //disable resize
        setResizable(false);
        setVisible(true);
    }

    /**
     * setup_buttons
     * This methods sets up all the actions for when each button is clicked
     * login button, register button
     */
    private void setup_buttons()
    {

        //---------Login Button--------------------------//
        buttonLogin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //get the username and password entered
                String username = userNameInput.getText();
                String password = passwdInput.getText();
                String cmd = "login";
                String line = cmd+":"+username+":"+password;

                //send them to the server for validation
                writeToServer.println(line);
                writeToServer.flush();

                //clear the user input fields
                userNameInput.setText("");
                passwdInput.setText("");
            }
        });

        //--------------Register Button-----------------------//
        buttonRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //get the username and password entered
                String username = userNameInput.getText();
                String password = passwdInput.getText();
                String cmd = "register";
                String line = cmd+":"+username+":"+password;

                //send them to the server
                sendMessageToServer(line);

                //clear the user input fields
                userNameInput.setText("");
                passwdInput.setText("");
            } });
    }

    /**
     * listenToServer
     * This methods listens to Server for incoming messages
     */
    private void listenToServer()
    {
        String response;

            while(isRunning)
            {
                try
                {
                    //get response from server
                    while ((response = inputFromServer.readLine()) != null)
                    {
                        //if account details were valid, close login clientj
                        if (response.equalsIgnoreCase("true"))
                        {
                            break;
                        }

                        //display error message, validation not successful j
                        else if(response.equalsIgnoreCase("false"))
                        {
                            displayErrorMessage();
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                isRunning = false;
         }

        try
        {
            //cleanup and close all socket connections
            connectionToServer.close();
            writeToServer.close();
            inputFromServer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        dispose();
    }

    /**
     * displayErrorMessage
     * This method displays a window with an error message stating invalid login
     */
    private void displayErrorMessage()
    {
        //display error message to user that invalid login details were submitted
        JOptionPane.showMessageDialog (null, "Invalid Username or Password (CASE SENSITIVE)", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * sendMessageToServer
     * This method sends a message to the Server
     * @param message - message to be sent
     */
    private void sendMessageToServer(String message)
    {
        writeToServer.println(message);
        writeToServer.flush();
    }

    /**
     * main
     * Runs the login client on a specified host and port number
     * @param args
     */
    public static void main(String [] args)
    {

        //setup host and port number to connect to
        String hostName = "localhost";
        int port = 4444;

        //launch the login client
        LoginClient Login = new LoginClient(hostName,port);
        Login.run();
    }
}
