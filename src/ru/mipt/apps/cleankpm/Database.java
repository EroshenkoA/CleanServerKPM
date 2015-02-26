package ru.mipt.apps.cleankpm;

import ru.mipt.apps.cleankpm.userObjects.User;
import ru.mipt.apps.cleankpm.userObjects.UserInitials;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by User on 2/23/2015.
 */
public class Database implements Serializable { //singleton class

    public final static String databasePath = Database.class.getClassLoader().getResource("//").getPath()+"ru\\mipt\\apps\\cleankpm\\resourses\\" + "serialized.out";
    private static boolean wasInitialized = false;
    private ArrayList<User> usersList;
    private static volatile Database instance;
    public static Database getInstance(){
        if (instance==null){
            instance = getInstanceFromFile();
        }
        return instance;
    }
    public User findUserByName(String name) {
        int lastIndex = usersList.size()-1;
        for (int i=0; i<=lastIndex; i++){
            if (name.compareTo( (usersList.get(i)).getName())==0){//case equal
                return usersList.get(i);
            }
        }
        return null;
    }
    public void addUser(UserInitials userInitials){
        usersList.add(new User(userInitials));
        try {
            save();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void initialize(){
        wasInitialized = true;
        //eventsList = new ArrayList<>();
        usersList = new ArrayList<User>();
        try {
            save();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void save() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(databasePath));
        oos.writeObject(this);
        oos.flush();
        oos.close();
    }
    private static Database getInstanceFromFile() {
        try {
            if (wasInitialized == false) {
                if (!((new File(databasePath)).exists())) {
                    Database database = new Database();
                    instance = database;
                    database.initialize();
                    return database;
                }
                Database database = (Database) (new ObjectInputStream(new FileInputStream(databasePath))).readObject();
                return database;
            }else{
                Database database = (Database) (new ObjectInputStream(new FileInputStream(databasePath))).readObject();
                return database;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private Database(){
    }

}
