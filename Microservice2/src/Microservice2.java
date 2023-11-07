import java.io.*;
import java.net.*;

// Server class 
class Microservice2 {
    private static boolean terminateServer = false;

    public static void main(String[] args)
    {
        ServerSocket server = null;
        Socket socketBaaS = null;
        try {
            server = new ServerSocket(1202);
            server.setReuseAddress(true);
            socketBaaS = new Socket("localhost", 2304);
            while (!terminateServer)  {
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

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private Socket BaaSSocket;
        private BufferedReader inBaas;
        private PrintWriter outBaas;

        // Constructor
        public ClientHandler(Socket socket,Socket socket2)
        {
            this.clientSocket = socket;
            try{
                BaaSSocket = socket2;
                outBaas = new PrintWriter(BaaSSocket.getOutputStream(), true);
                inBaas = new BufferedReader(new InputStreamReader(BaaSSocket.getInputStream()));
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
                    line=line.toLowerCase();
                    msg=parts[0]+";;"+parts[1]+";;"+"Text:"+line;

                    outBaas.println(msg);
                    outBaas.flush();

                    msg = inBaas.readLine();
                    System.out.println("Server replied "
                            + msg);
                    out.println(msg);
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
