import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args)
    {
        try (Socket socket = new Socket("localhost", 1234)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner sc = new Scanner(System.in);
            String line = null,msg;
            while (!"exit".equalsIgnoreCase(line)) {
                Random rand=new Random();
                int id=rand.nextInt(1000,9999);

                System.out.println("Wyślij pierwszą wiadomość do serwera!!!");
                line = sc.nextLine();
                if("exit".equalsIgnoreCase(line)){
                    break;
                }
                msg = "Type: msg_service1;;Message_id: "+id+";;Text:"+line;
                out.println(msg);
                out.flush();

                System.out.println("Wyślij drugą wiadomość do serwera!!!");
                line = sc.nextLine();
                if("exit".equalsIgnoreCase(line)){
                    break;
                }
                msg = "Type: msg_service2;;Message_id: "+id+";;Text:"+line;
                out.println(msg);
                out.flush();

                msg = in.readLine();
                String[] parts=msg.split(";;");
                line=parts[3].split(":")[1];
                System.out.println("Odpowiedź:"+line);
            }
            sc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}