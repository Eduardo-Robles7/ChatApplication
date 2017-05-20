import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by eduardorobles on 5/13/17.
 */
public class User
{
    private String user_name;
    private String password;

    public User()
    {
        this.user_name = null;
        this.password = null;
    }
    public User(String user_name, String password)
    {
        this.user_name = user_name;
        this.password  = password;
    }

    public User(User otherUser)
    {
        this.user_name = otherUser.user_name;
        this.password = otherUser.password;
    }

    public void copy(User otherUser)
    {
        this.user_name = otherUser.user_name;
        this.password = otherUser.password;
    }

    public String getUser_name()
    {
        return user_name;
    }

    public void setUser_name(String user_name)
    {
        this.user_name = user_name;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

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

    private void add(User newUser)
    {
        map.put(newUser.getUser_name(),newUser);
    }

    public void addNewUser(String user_name,String password)
    {
        User newUser = new User(user_name,password);
        map.put(newUser.getUser_name(),newUser);
        addUserToFile(newUser,file);
    }
    public void addNewUser(User newUser)
    {
        map.put(newUser.getUser_name(),newUser);
        addUserToFile(newUser,file);
    }

    public boolean findAccount(String user_name,String password)
    {
        if(map.containsKey(user_name))
        {
            if(password.equals(map.get(user_name).getPassword()))
                return true;
            else
                return false;
        }
        return false;
    }

    public boolean copyAccount(String user_name,User newUser)
    {
        if(map.containsKey(user_name))
        {
            newUser.setUser_name(map.get(user_name).getUser_name());
            newUser.setPassword(map.get(user_name).getPassword());
            return true;
        }
        return false;
    }
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

    public void display_users()
    {
        for(String user_name : map.keySet())
        {
            System.out.println(user_name);
        }
    }

    public static void main(String [] args)
    {
        AccountManager Mananger = new AccountManager();

        User newUser = new User();

        Mananger.copyAccount("Robles5",newUser);

        newUser.display();
        Mananger.display_users();
    }
}
