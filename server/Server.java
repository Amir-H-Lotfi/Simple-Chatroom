package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
public class Server {

    public static void main(String[] args) throws IOException {

        System.out.println(
                "server is running ... "
        );
        int port = 15000;
        int maxConnection = 10;

        // creating thread pool for every client which connected to this server
        Executor executor = Executors.newFixedThreadPool(10);

        // wait and listen to socket at port 15000
        ServerSocket serverSocket = new ServerSocket(port, maxConnection);
        while (true) {
            try {

                // accept a connection from client
                Socket connection = serverSocket.accept();

                // create a shadow form of client for handling its message in server
                ClientHandler model = new ClientHandler(connection);

                // each shadow or model are kind of thread, so we send the to execute pool
                executor.execute(model);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
