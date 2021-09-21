import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

public class ClientExample
{
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;
    ByteBuffer readBuffer = ByteBuffer.allocate(100);
    ByteBuffer writeBuffer = ByteBuffer.allocate(100);
    boolean loggedIn = false;
    String userId = "not set";
    List<Integer> reqIdList = new Vector<>((int) Math.pow(256,3));


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
                int reqId = availableReqId(0);
                send(reqId,0,name,-1);

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

    void processOutput(int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            int reqNum = reqIdList.get(reqId);
            reqIdList.add(reqId,0);
            System.out.println("우선 로그인 됨");

        }
        else if (serverResult == -1)
        {
            System.out.println("failed");
        }
    }


    void startClient()
    {
        try
        {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );

            socketChannel = AsynchronousSocketChannel.open(channelGroup);
            socketChannel.connect(new InetSocketAddress("localhost", 5001), null, new CompletionHandler<Void, Object>()
            {
                @Override
                public void completed(Void result, Object attachment)
                {
                    try
                    {
                        System.out.println("[연결완료: " + socketChannel.getRemoteAddress() + "]");
                        for(int i = 0; i<reqIdList.size(); i++)
                        {
                            reqIdList.add(-1);
                        }
                    }
                    catch (IOException e){}
                    receive();
                }
                @Override
                public void failed(Throwable exc, Object attachment)
                {
                    System.out.println("[서버 통신 안됨]");
                    if(socketChannel.isOpen()) stopClient();
                }
            });
        }
        catch (IOException e){}
    }

    void stopClient()
    {
        System.out.println("[연결 끊음]");
        if(channelGroup != null && !channelGroup.isShutdown()) channelGroup.shutdown();
    }

    void receive()
    {
        socketChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>()
        {
            @Override
            public void completed(Integer result, ByteBuffer attachment)
            {
                try
                {
                    attachment.flip();
                    byte[] reqIdReceive = new byte[4];
                    byte[] resultReceive = new byte[4];
                    byte[] listReceive = new byte[4];
                    ByteBuffer reqIdByte = attachment.get(reqIdReceive, 0, 4);
                    int reqId = Integer.parseInt(reqIdByte.toString());
                    attachment.position(4);
                    ByteBuffer resultByte = attachment.get(resultReceive, 4, 4);
                    int serverResult = Integer.parseInt(resultByte.toString());
                    attachment.position(8);
                    ByteBuffer data = attachment.get(listReceive, 8, 4);
                    attachment.position(12);
                    processOutput(reqId,serverResult,data);

                    readBuffer.clear();
                    socketChannel.read(readBuffer,readBuffer,this);
                }
                catch(Exception e) {}
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                System.out.println("[서버 통신 안됨]");
                stopClient();
            }
        });
    }

    void send(int reqId,int reqNum , String userId,int roomNum)
    {
        writeBuffer.put((byte)reqId);
        writeBuffer.position(4);
        writeBuffer.put((byte) reqNum);
        writeBuffer.position(8);
        writeBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
        writeBuffer.position(24);
        writeBuffer.put((byte) roomNum);
        writeBuffer.position(28);
        writeBuffer.flip();
        socketChannel.write(writeBuffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                System.out.println("[보내기 완료]"+ result);

                writeBuffer.clear();
            }

            @Override
            public void failed(Throwable exc, Object attachment)
            {
                System.out.println("[서버 통신 안됨]");
                stopClient();
            }
        });
    }

    int availableReqId(int reqNum)
    {
        for (int i = 0; i<reqIdList.size(); i++)
        {
            if (reqIdList.get(i) == -1)
            {
                reqIdList.add(i,reqNum);
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args)
    {
        ClientExample clientExample = new ClientExample();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            clientExample.startClient();
            String input;
            while((input = br.readLine()) != null)
            {
                clientExample.processInput(input);
            }
        }
        catch(IOException e){}
    }

}
