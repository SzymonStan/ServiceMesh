import java.io.*;
import java.net.*;

class APIGateway {
    public static void main(String[] args)
    {
        ServerSocket server = null;
        Socket micro1 = null;
        Socket micro2 = null;
        try {
            server = new ServerSocket(1234);
            server.setReuseAddress(true);
            micro1 = new Socket("localhost", 1201);
            micro2 = new Socket("localhost", 1202);

            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected to " + client.getInetAddress().getHostAddress());


                ClientHandler clientSock = new ClientHandler(client,micro1,micro2);
                new Thread(clientSock).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private Socket sock1;
        private Socket sock2;
        private BufferedReader in2;
        private PrintWriter out1;
        private PrintWriter out2;

        // Constructor
        public ClientHandler(Socket socket,Socket service1, Socket service2)
        {
            this.clientSocket = socket;
            sock1=service1;
            sock2=service2;
            try{
                out1 = new PrintWriter(sock1.getOutputStream(), true);
                out2 = new PrintWriter(sock2.getOutputStream(), true);
                in2 = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void run()
        {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.printf(" Sent from the client: %s\n", msg);
                    String[] parts=msg.split(";;");
                    String type=parts[0].split(" ")[1];
                    if(type.equals("msg_service1")){
                        out1.println(msg);
                        out1.flush();
                    } else if (type.equals("msg_service2")){
                        out2.println(msg);
                        out2.flush();
                        msg = in2.readLine();
                        out.println(msg);
                    } else{
                        out.println("Błąd!!!");
                    }
                }
            } catch (SocketException se) {
                System.out.println("Client disconnected abruptly: " + se.getMessage());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
