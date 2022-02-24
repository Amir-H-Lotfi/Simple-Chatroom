package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * take this class as a thread which handle all connected client to this server
 */

public class ClientHandler implements Runnable {

    // each shadow have username and ac connected tcp socket
    private String userName = "";
    private Socket socket;

    // input and output stream for handling user inputs and output data
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    //  object like secretary which handle the messaging protocol functions like joining , removing,sending public and private message
    private MessageMediator messageMediator = MessageMediator.getInstance();

    // constructor
    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        printWriter = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }


    public void closeConnection() throws IOException {
        printWriter.close();
        bufferedReader.close();
        socket.close();
    }

    /**
     * function which read the input data from socket  ... and interpret them as protocol functions ...
     * each protocol function will handle in separate if else block ... every if else block will invoke related protocol function
     * which implemented in @Class server.MessageMediator
     */
    @Override
    public void run() {
        try {
            while (true) {
                String command = "";
                command = bufferedReader.readLine().trim();

                // handle hello input message ... we do not need username

                if (command.startsWith("Hello") && userName.equals("")) {
                    int dotIndex = command.indexOf(".");
                    int spaceIndex = command.indexOf(" ");
                    String userName = command.substring(spaceIndex + 1, dotIndex).trim();
                    setUserName(userName);
                    messageMediator.addUser(this);
                }
                // if we have valid username ...
                if (!userName.equals("")) {
                    // handle user lise message ...
                    if (command.equals("please send the list of attendees.")) {
                        messageMediator.getLiveUsers(this);
                    }
                    // handle bye message and leave the chat room
                    else if (command.equals("Bye.")) {
                        messageMediator.removeUser(this);
                        return;
                    }

                    //handle public message
                    else if (command.startsWith("public message")) {

                        int assignIndex = command.indexOf("=");
                        int colonIndex = command.indexOf(":");

                        int len = Integer.parseInt(command.substring(assignIndex + 1, colonIndex).trim());

                        char[] buf = new char[len];
                        bufferedReader.read(buf);
                        messageMediator.sendPublicMsg(this, buf);

                    }
                    // handle private message

                    else if (command.startsWith("private message")) {
                        int assignIndex = command.indexOf("=");
                        int toIndex = command.indexOf(" to ");

                        int len = Integer.parseInt(command.substring(assignIndex + 1, toIndex).trim());

                        int colonIndex = command.indexOf(":");
                        String users = command.substring(toIndex + " to ".length(), colonIndex).trim();

                        ArrayList<String> userList = new ArrayList<>();
                        StringTokenizer tokenizer = new StringTokenizer(users, ",");

                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            userList.add(token);
                        }

                        // allocate character buffer
                        char[] buf = new char[len];
                        bufferedReader.read(buf);
                        messageMediator.sendPrivateMsg(this, userList.toArray(new String[0]), buf);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
