import java.io.*;
import java.net.*;

// Server class 
class Microservice1 {
    private static boolean terminateServer = false;

    public static void main(String[] args)
    {
        ServerSocket server = null;
        Socket socketBaaS = null;
        try {
            server = new ServerSocket(1201);
            server.setReuseAddress(true);
            socketBaaS = new Socket("localhost", 2303);
            while (!terminateServer) {
                Socket client = server.accept();
                System.out.println("New client connected" + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client,socketBaaS);
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

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private Socket BaaSSocket;
        private PrintWriter outBaas;

        // Constructor
        public ClientHandler(Socket socket,Socket socket2)
        {
            this.clientSocket = socket;
            try{
                BaaSSocket = socket2;
                outBaas = new PrintWriter(BaaSSocket.getOutputStream(), true);
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

                String line,msg;
                while ((msg = in.readLine()) != null) {
                    System.out.printf(" Sent from the client: %s\n", msg);
                    if (msg.equals("exit")) {
                        terminateServer = true;
                        break;
                    }

                    String[] parts=msg.split(";;");
                    line=parts[2].split(":")[1];
                    String[] words = line.split(" ");
                    StringBuilder reversedSentence = new StringBuilder();
                    for (int i = words.length - 1; i >= 0; i--) {
                        reversedSentence.append(words[i]);
                        if (i > 0) {
                            reversedSentence.append(" ");
                        }
                    }
                    line=reversedSentence.toString();

                    msg=parts[0]+";;"+parts[1]+";;"+"Text:"+line;
                    outBaas.println(msg);
                    outBaas.flush();
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