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
    ByteBuffer readBuffer = ByteBuffer.allocate((int) Math.pow(2,20));
    ByteBuffer writeBuffer = ByteBuffer.allocate((int) Math.pow(2,20));
    boolean loggedIn = false;
    String userId = "not set";



    void processInput(String command)
    {
        if(command.charAt(0) == '\\') // if it is command
        {
            if(command.startsWith("login", 1))
            {
                if (loggedIn == true)
                {
                    System.out.println("already logged in");
                    return;
                }
                String name = command.substring(7);
                userId = name;
                send(0,name);

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

    void processOutput(int success,int op, String data)
    {
        if(op == 0)
        {
            if(success == -1)
            {
                userId="not set";
            }
            System.out.println(data);
        }
    }


    void startClientService()
    {
        Thread thread = new Thread(() ->
        {
            try
            {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(true);
                socketChannel.connect(new InetSocketAddress("localhost",5001));
                System.out.println("[연결 완료: " + socketChannel.getRemoteAddress() + "]");
                System.out.print("로그인 해주세요 : ");
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
                System.out.println("입력을 기다리는중...");
                int readByteCount = socketChannel.read(readBuffer);
                System.out.println(readByteCount);
                if(readByteCount == -1) throw new IOException();
                readBuffer.flip();
                int success = readBuffer.get(0);
                int op = readBuffer.get(1);
                readBuffer.position(2);
                Charset charset = Charset.forName("UTF-8");
                String data = charset.decode(readBuffer).toString();
                processOutput(success,op,data);
                readBuffer.clear();
                System.out.println("[답장받음]");
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
                writeBuffer.put((byte)op);
                writeBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
                ByteBuffer toSend = writeBuffer.flip();
                socketChannel.write(toSend);
                writeBuffer.clear();
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
            client.startClientService();
            String input;
            while((input = br.readLine()) != null)
            {
                client.processInput(input);
            }
        }
        catch(IOException e){}
    }

}
