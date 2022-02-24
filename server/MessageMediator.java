package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * this class implemented as singleton ... and also contain mediator ... for implementing core protocol function like sending message
 */
public class MessageMediator {

    // set of live user
    private final static Map<String, ClientHandler> modelMap = new ConcurrentHashMap();

    private final static MessageMediator instance = new MessageMediator();

    private MessageMediator() {

    }

    public synchronized static MessageMediator getInstance() {
        return instance;
    }


    // implementing add function of protocol
    public synchronized void addUser(ClientHandler me)
            throws IOException {
        String userName = me.getUserName();
        modelMap.put(userName, me);
        String responseMsgToAll = userName + " join the chat room.";
        String responseMsgToMe = "Hi " + userName + ",welcome to the chat room.";

        me.getPrintWriter().println(responseMsgToMe);

        modelMap.values().forEach(other -> {
            PrintWriter otherWriter = other.getPrintWriter();
            otherWriter.println(responseMsgToAll);
        });


        // logging to stdout

        System.out.println(String.format("server:log[user : %s, request : join to chat room]",me.getUserName()));
        System.out.println("$add successfully$");
        System.out.println("----end----");
        System.out.println();
    }

    // implementing userList function of protocol
    public synchronized void getLiveUsers(ClientHandler me) {
        StringJoiner joiner = new StringJoiner(",");
        PrintWriter writer = me.getPrintWriter();
        modelMap.keySet().forEach(userName -> joiner.add(userName));
        String users = joiner.toString();
        String responseMsgToMe = "Here is the list of attendees:";
        writer.println(responseMsgToMe);
        writer.println(users);

        // logging to stdout

        System.out.println(String.format("server:log[user : %s, request :user list]", me.getUserName()));
        System.out.println("$current user list$ ");
        System.out.println(users);
        System.out.println("----end----");
        System.out.println();


    }

    // implementing remove function of protocol
    public synchronized void removeUser(ClientHandler me) throws IOException {
        String userName = me.getUserName();
        boolean remove = modelMap.remove(userName, me);
        String responseMsgToAll = userName + " left the chat room.";
        if (remove) {
            modelMap.values().forEach(other -> {
                PrintWriter otherWriter = other.getPrintWriter();
                otherWriter.println(responseMsgToAll);
            });
        }
        me.getPrintWriter().println("Bye.");
        me.closeConnection();

        // logging to stdout

        System.out.println(String.format("server:log[user : %s, request :leave chat room]", me.getUserName()));
        System.out.println("$removed successfully$");
        System.out.println("----end----");
        System.out.println();
    }

    // implementing public message function of protocol

    public synchronized void sendPublicMsg(ClientHandler me, char[] msg) {
        String publicMsgHeader =
                "Public Message from " +
                        me.getUserName() +
                        ",length=" + msg.length + ":";

        modelMap.values().stream().filter(model -> !model.equals(me)).forEach(model -> {
            PrintWriter modelWriter = model.getPrintWriter();
            modelWriter.println(publicMsgHeader);
            modelWriter.print(msg);
            modelWriter.flush();
        });

        // logging to stdout

        System.out.println(String.format("server:log[user : %s, request :send public message]", me.getUserName()));
        System.out.println("$header$ " + publicMsgHeader);
        System.out.println("$message$ " + msg);
        System.out.println("----end----");
        System.out.println();

    }

    // implementing private message function of protocol

    public synchronized void sendPrivateMsg(ClientHandler me, String[] toModels, char[] msg) {
        StringJoiner joiner = new StringJoiner(",");
        for (String model : toModels) joiner.add(model);
        String privateMsgHeader = "Private Message,length=" + msg.length + "from "
                + me.getUserName() + " to " + joiner.toString() + ":";
        for (String model : toModels) {
            if (modelMap.containsKey(model)) {
                PrintWriter modelWriter = modelMap.get(model).getPrintWriter();
                modelWriter.println(privateMsgHeader);
                modelWriter.print(msg);
                modelWriter.flush();
            }
        }

        // logging to stdout
        System.out.println(String.format("server:log[user : %s, request :send private message]", me.getUserName()));
        System.out.println("$header$ " + privateMsgHeader);
        System.out.println("$message$ " + msg);
        System.out.println("----end----");
        System.out.println();
    }

}
