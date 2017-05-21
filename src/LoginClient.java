import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
/**
 * Created by eduardorobles on 5/19/17.
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

    public LoginClient(String hostName, int portNumber)
    {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.isRunning = true;
    }

    public void run()
    {
        createGUI();
        setup_streams();
        setup_buttons();
        listenToServer();
    }
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
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //set display size
        setSize(300,100);

        //center the frame to middle of screen
        setLocationRelativeTo(null);
        setTitle("Ping Chat Login");
        //disable resize
        setResizable(false);
        setVisible(true);
    }

    private void setup_buttons()
    {
        buttonLogin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String username = userNameInput.getText();
                String password = passwdInput.getText();
                String cmd = "login";
                String line = cmd+":"+username+":"+password;
                writeToServer.println(line);
                writeToServer.flush();
                userNameInput.setText("");
                passwdInput.setText("");
            }
        });
    }

    private void listenToServer()
    {
        String response;

            while(isRunning)
            {
                try
                {
                    while ((response = inputFromServer.readLine()) != null)
                    {
                        if (response.equalsIgnoreCase("true"))
                        {
                            isRunning = false;
                            break;
                        }
                        else if(response.equalsIgnoreCase("false"))
                            displayErrorMessage();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
        try {
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

    private void displayErrorMessage()
    {
        JOptionPane.showMessageDialog (null, "Message", "Title", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String [] args)
    {
        String hostName = "localhost";
        int port = 4444;

        LoginClient Login = new LoginClient(hostName,port);
        Login.run();
    }
}
