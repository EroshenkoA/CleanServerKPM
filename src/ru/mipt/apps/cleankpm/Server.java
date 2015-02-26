package ru.mipt.apps.cleankpm;

import ru.mipt.apps.cleankpm.constants.Config;
import ru.mipt.apps.cleankpm.statics.BufWrapper;
import ru.mipt.apps.cleankpm.tabObjects.Event;
import ru.mipt.apps.cleankpm.userObjects.User;
import ru.mipt.apps.cleankpm.userObjects.UserInitials;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by User on 2/9/2015.
 */
public class Server {
    public static void main(String[] args) throws Throwable {
        ServerSocket ss = new ServerSocket(Config.port);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            new Thread(new SocketProcessor(s)).start();
        }
    }

    private static class SocketProcessor implements Runnable {
        private Socket s;
        private ObjectInputStream is;
        private ObjectOutputStream os;
        private byte[] buf;
        private int command;
        private Lock lock = new ReentrantLock();

        private SocketProcessor(Socket s) throws Throwable {
            this.s = s;
            this.is = new ObjectInputStream(s.getInputStream());
            this.os = new ObjectOutputStream(s.getOutputStream());
            setBuf();
        }
        private void setBuf(){
            buf = BufWrapper.setSocketPreBuf();
        }
        private void endStreams(){
            try {
                os.flush();
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() { //lock needed because of save() operation I suppose
            try {
                is.read(buf);
                command = BufWrapper.convertToInt(buf);
                UserInitials userInitials;
                switch(command) {

                    case Config.SIGN_IN:{
                        userInitials = (UserInitials) (is.readObject());
                        lock.lock();
                        Database database = Database.getInstance();
                        User databaseUser = database.findUserByName(userInitials.getName());
                        lock.unlock();
                        if (databaseUser == null) {
                            System.out.println("there is no " + userInitials.getName() + " in database");
                            buf=BufWrapper.convertToBuf(Config.DOESNOT_EXIST);
                            os.write(buf);
                        } else if ((databaseUser.getPassword()).compareTo(userInitials.getPassword())!=0 ) {
                            System.out.println(userInitials.getName() + " tries wrong password");
                            buf=BufWrapper.convertToBuf(Config.WRONG_PASSWORD);
                            os.write(buf);
                        } else { //case correct
                            System.out.println(userInitials.getName() + " may be logged in");
                            buf=BufWrapper.convertToBuf(Config.SIGNED_IN);
                            os.write(buf);
                            os.writeObject(databaseUser);
                        }
                        endStreams();
                        break;
                    }

                    case Config.SIGN_UP: {
                        userInitials = (UserInitials) (is.readObject());
                        lock.lock();
                        Database database = Database.getInstance();
                        User databaseUser = database.findUserByName(userInitials.getName());
                        if (databaseUser == null) {
                            database.addUser(userInitials);
                            lock.unlock();
                            System.out.println("User " + userInitials.getName() + " registered");
                            buf=BufWrapper.convertToBuf(Config.SIGNED_IN);
                            os.write(buf);
                            os.writeObject(database.findUserByName(userInitials.getName()));
                        } else {
                            lock.unlock();
                            System.out.println("User " + userInitials.getName() + " is already in database and can't be registered");
                            buf=BufWrapper.convertToBuf(Config.NAME_EXISTS);
                            os.write(buf);
                        }

                        endStreams();
                        break;
                    }
                    case Config.ADD_EVENT: {
                        Event event = (Event) (is.readObject());
                        lock.lock();
                        Database database = Database.getInstance();
                        if ((database.findEventByName(event.getEventName()))==null){
                            database.addEvent(event);
                            buf = BufWrapper.convertToBuf(Config.OK);
                        }else{
                            System.out.println("such name already exists");
                            buf = BufWrapper.convertToBuf(Config.NAME_EXISTS);
                        }
                        lock.unlock();
                        os.write(buf);
                        endStreams();
                        break;
                    }
                    case Config.UPDATE_USER:{
                        break;
                    }

                }
            } catch (Throwable t) {
                t.printStackTrace();
                /*do nothing*/
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished");
        }

    }
}
