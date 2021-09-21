import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

public class ClientExample
{
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;
    ByteBuffer readBuffer = ByteBuffer.allocate(100);
    ByteBuffer writeBuffer = ByteBuffer.allocate(100);
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
                    Charset charset = Charset.forName("UTF-8");
                    String data = charset.decode(attachment).toString();
                    System.out.println(data);
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

    void send(String data)
    {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(data);
        writeBuffer.put(byteBuffer);
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
                clientExample.send(input);
            }
        }
        catch(IOException e){}
    }

}
