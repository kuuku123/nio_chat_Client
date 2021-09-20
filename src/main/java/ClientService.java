import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;


public class ClientService
{
    SocketChannel socketChannel;
    List<Client> clients = new Vector<>();

    class Client
    {
        String userId;
        List<Integer> rooms = new Vector<>();
        int cur_room;
    }



    void processCommand(String command)
    {
        if(command.charAt(0) == '\\') // if it is command
        {
            if(command.startsWith("login", 1))
            {
                Client client = new Client();
                client.userId = command.substring(7);
                clients.add(client);
                startClientService(0,client);
            }
            else if(command.startsWith("logout", 1))
            {

            }
            else if(command.startsWith("uploadfile", 1))
            {

            }
            else if(command.startsWith("showfile", 1))
            {

            }
            else if(command.startsWith("downloadfile", 1))
            {

            }
            else if(command.startsWith("deletefile", 1))
            {

            }
            else if(command.startsWith("createroom", 1))
            {
                if(command.length() == "createroom".length())
                {

                }
                else
                {

                }

            }
            else if(command.startsWith("exitroom", 1))
            {

            }
            else if(command.startsWith("inviteuser", 1))
            {

            }
            else if(command.startsWith("banuser", 1))
            {

            }
            else if(command.startsWith("enterroom", 1))
            {

            }
            else if(command.startsWith("showuser", 1))
            {

            }
            else if(command.startsWith("showroom", 1))
            {

            }

        }

    }

    void startClientService(int op,Client client)
    {
        Thread thread = new Thread(() ->
        {
            try
            {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(true);
                socketChannel.connect(new InetSocketAddress("localhost",5001));
                System.out.println("[연결 완료: " + socketChannel.getRemoteAddress() + "]");
                System.out.println("이제 입력가능합니다.");
                System.out.println("채팅을 하고싶으면 \\createroom으로 방을 만들고해주세요");
                send(op,client.userId);
            }
            catch(Exception e)
            {
                System.out.println("[서버 통신 안됨]");
                if(socketChannel.isOpen()) stopClient();
                return;
            }
            receive();
        });
        thread.start();
    }
    void stopClient()
    {
        try
        {
            System.out.println("[연결 끊음]");
            if(socketChannel!=null && socketChannel.isOpen()) socketChannel.close();
        }
        catch(IOException e){}
    }
    void receive()
    {
        while(true)
        {
            try
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(10000);
                int readByteCount = socketChannel.read(byteBuffer);
                if(readByteCount == -1) throw new IOException();
                byteBuffer.flip();
                System.out.println(byteBuffer);
                Charset charset = Charset.forName("UTF-8");
                String data = charset.decode(byteBuffer).toString();
                System.out.println("답장받음");
                System.out.println(data);
            }
            catch(Exception e)
            {
                System.out.println("[서버 통신 안됨] receive");
                stopClient();
                break;
            }
        }
    }
    void send(int op,String userId)
    {
        Thread thread = new Thread(() ->
        {
            try
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate((int) Math.pow(2, 20));
                byteBuffer.put((byte)op);
                byteBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
                ByteBuffer toSend = byteBuffer.flip();
                socketChannel.write(toSend);
                System.out.println("[보내기 완료]");
            }
            catch(Exception e)
            {
                System.out.println("[서버 통신 안됨 , 보내기 error]");
                stopClient();
            }
        });
        thread.start();
    }

    public static void main(String[] args)
    {
        ClientService client = new ClientService();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            System.out.print("로그인 해주세요 : ");
            String input;
            while((input = br.readLine()) != null)
            {
                client.processCommand(input);
                System.out.print("입력해주세요 : ");
            }
        }
        catch(IOException e){}
    }

}
