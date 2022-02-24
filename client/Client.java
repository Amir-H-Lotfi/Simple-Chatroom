package client;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Client {

    private static BufferedReader stdReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // main information for connection to server ... so we need port number and host name
        int port = 15000;
        InetAddress host = InetAddress.getLocalHost();

        Socket socket = new Socket(host, port);

        // connection accepted
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        // get input and output stream from connection
        // each stream will handle by some part of program
        InputHandler inputHandler = new InputHandler(inputStream);
        OutputHandler outputHandler = new OutputHandler(outputStream);


        // execute thread for handling input and output stream from connection socket
        executor.execute(inputHandler);
        executor.execute(outputHandler);
        executor.shutdown();
    }

    /**
     * main class for handling input stream ... we record input stream into file ... as log file
     */
    public static class InputHandler implements Runnable {
        private InputStream inputStream;

        public InputHandler(InputStream inputStream)
                throws IOException {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line.startsWith("Public")) {

                        int assignIndex = line.indexOf("=");
                        int colonIndex = line.indexOf(":");

                        int len = Integer.parseInt(line.substring(assignIndex + 1, colonIndex).trim());
                        char[] buf = new char[len];
                        reader.read(buf);
                        // log to stdout
                        System.out.println("client:log[public message]");
                        System.out.println("$header$");
                        System.out.println(line);
                        System.out.println("$message$");
                        System.out.println(new String(buf));
                        System.out.println("----end----");
                        System.out.println();

                    } else if (line.startsWith("Private")) {
                        int assignIndex = line.indexOf("=");
                        int fromIndex = line.indexOf("from");

                        int len = Integer.parseInt(line.substring(assignIndex + 1, fromIndex).trim());
                        char[] buf = new char[len];
                        reader.read(buf);

                        // log to stdout
                        System.out.println("client:log[private message]");
                        System.out.println("$header$");
                        System.out.println(line);
                        System.out.println("$message$");
                        System.out.println(new String(buf));
                        System.out.println("----end----");
                        System.out.println();

                    } else if (line.startsWith("Here is the list")) {
                        System.out.println("client:log[user list]");
                        System.out.println("$list$");
                        System.out.println(reader.readLine());
                        System.out.println("----end----");

                    } else if (line.startsWith("Bye")) {
                        System.out.println(line);
                        System.out.println("----end----");
                        System.out.println();
                        return;
                    } else if (line.startsWith("Hi")) {
                        System.out.println("client:log[join message]");
                        System.out.println("$message$");
                        System.out.println(line);
                        System.out.println("$broadcast$");
                        System.out.println(reader.readLine());
                        System.out.println("----end----");

                        System.out.println();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return ;
                }
            }
        }
    }

    /**
     * main class for handling output stream ... so in this class we write down out message
     */
    public static class OutputHandler implements Runnable {
        private PrintWriter outputWriter;

        public OutputHandler(OutputStream outputStream) {
            outputWriter = new PrintWriter(
                    new OutputStreamWriter(
                            outputStream
                    ), true
            );
        }

        private void menu() {
            System.out.println();
            System.out.println("commands >> ");
            System.out.println("1.bye.");
            System.out.println("2.list of attendees.");
            System.out.println("3.public message.");
            System.out.println("4.private message.");
            System.out.println();
        }

        @Override
        public void run() {
            try {

                String command, msg, header;

                System.out.println("Enter your user name :");
                command = stdReader.readLine().trim();
                System.out.println();
                msg = "Hello " + command.trim() + ".";
                outputWriter.println(msg);


                // read the command that showed in menu
                while (true) {
                    menu();
                    command = stdReader.readLine().trim();
                    switch (command) {

                        case "1":// case for bye message
                            msg = "Bye.";
                            outputWriter.println(msg);
                            return;
                        case "2":// case for live user list
                            msg = "please send the list of attendees.";
                            outputWriter.println(msg);
                            break;
                        case "3":// public message
                        case "4":// private message

                            String val = command;
                            System.out.println("Enter your message (enter * in separate line for ending):");
                            StringBuilder msgBuilder = new StringBuilder(1024);
                            while (true) {
                                command = stdReader.readLine();
                                if (command.equals("*")) break;
                                msgBuilder.append(command + "\r\n");
                            }

                            msg = msgBuilder.toString();

                            //create public message header
                            if (val.equals("3")) {
                                header = "public message,length=" + msg.length() + ":";
                            }// create the private message header ...


                            else {
                                System.out.println("Enter receiver (enter * in separate line for ending):");
                                StringJoiner userBuilder = new StringJoiner(",");
                                while (true) {
                                    command = stdReader.readLine();
                                    if (command.equals("*")) break;
                                    else userBuilder.add(command);
                                }
                                header = "private message,length=" + msg.length() + " to " + userBuilder.toString() + ":";
                            }

                            // send header and message out
                            outputWriter.println(header);
                            outputWriter.print(msg);
                            outputWriter.flush();
                            break;
                        default:
                            System.out.println("invalid commands");
                            break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                return ;
            }
        }
    }
}
