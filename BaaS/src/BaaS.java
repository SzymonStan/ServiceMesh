import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

public class BaaS {
    protected static String readLine1=null;
    protected static String readLine2=null;
    private static boolean terminateServer = false;
    public static void main(String[] args) {
        List<ServerSocketChannel> servers = new ArrayList<>();
        Selector selector = null;

        try {
            selector = Selector.open();

            int[] ports = {2303, 2304};

            for (int port : ports) {
                ServerSocketChannel server = ServerSocketChannel.open();
                server.configureBlocking(false);
                server.socket().bind(new InetSocketAddress(port));

                server.register(selector, SelectionKey.OP_ACCEPT);

                servers.add(server);
            }

            while (!terminateServer) {
                selector.select();
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel socket = server.accept();
                        Socket client = socket.socket();
                        System.out.println("New client connected to " + client.getInetAddress().getHostAddress());

                        ClientHandler clientSock = new ClientHandler(client);
                        new Thread(clientSock).start();
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (ServerSocketChannel server : servers) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.printf("Sent from the client: %s\n", line);
                    if (line.equals("exit")) {
                        terminateServer = true;
                        break;
                    }
                    if(BaaS.readLine1==null){
                        BaaS.readLine1=line;
                    } else {
                        BaaS.readLine2=line;
                        String[] parts=BaaS.readLine1.split(";;");
                        String line1=parts[2].split(":")[1];
                        parts=BaaS.readLine2.split(";;");
                        String line2=parts[2].split(":")[1];
                        line=parts[0]+";;"+parts[1]+";;Status: 200;;Text:"+line1+" "+line2;
                        BaaS.readLine1=null;
                        BaaS.readLine2=null;
                        out.println(line);
                    }
                }
            } catch (SocketException se) {
                System.out.println("Client disconnected abruptly: " + se.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
