import util.LogFormatter;
import util.Operation;

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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientExample
{
    private final static Logger logr = Logger.getGlobal();
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;
    ByteBuffer readBuffer = ByteBuffer.allocate(1000);
    ByteBuffer writeBuffer = ByteBuffer.allocate(1000);
    boolean loggedIn = false;
    String userId = "not set";
    List<Integer> reqIdList = new Vector<>((int) Math.pow(256,3));

    private static void setupLogger()
    {
        LogFormatter formatter = new LogFormatter();
        LogManager.getLogManager().reset();
        logr.setLevel(Level.ALL);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(formatter);
        logr.addHandler(ch);


    }
    void processInput(String command)
    {
        if(command.charAt(0) == '\\') // if it is command
        {
            if(command.startsWith("login", 1))
            {
                if (loggedIn == true)
                {
                    logr.severe("already logged in");
                    return;
                }
                startClient();
                synchronized (logr)
                {
                    try
                    {
                        logr.wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                String name = command.substring(7);
                userId = name;
                int reqId = availableReqId(0);
                send(reqId,0,name,-1);

            }
            else if(command.startsWith("logout", 1))
            {
                if (loggedIn == true)
                {
                    int reqId = availableReqId(1);
                    send(reqId,1,userId,-1);
                }
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

    void processOutput(int reqId, int serverResult, String data)
    {
        Operation op = Operation.fromInteger(reqIdList.get(reqId));
        switch (op)
        {
            case login:
                loginProcess(op,reqId,serverResult, data);
                return;
            case logout:
                logoutProcess(op,reqId,serverResult,data);
                return;
            case sendText:
            case fileUpload:
            case fileList:
            case fileDownload:
            case fileDelete:
            case createRoom:
            case quitRoom:
            case inviteRoom:
            case requestQuitRoom:
            case roomUserList:
        }
    }

    void loginProcess(Operation op, int reqId, int serverResult, String data)
    {
        if (serverResult == 0)
        {
            reqIdList.add(reqId,-1);
            loggedIn = true;
            logr.info(op.toString()+" 성공함");
            logr.info("[requestId: "+reqId+" "+op+ " 성공함]");
        }
        else if (serverResult == -1)
        {
            logr.severe("requestId: "+reqId+" : " +op +" failed");
        }
    }

    void logoutProcess(Operation op, int reqId, int serverResult, String data)
    {
        try
        {
            socketChannel.close();
            reqIdList.add(reqId,-1);
            userId = "not set";
            loggedIn = false;
            logr.info("[서버와 연결종료]");
        } catch (IOException e)
        {
            e.printStackTrace();
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
                        logr.info("[연결완료: " + socketChannel.getRemoteAddress() + "]");
                        for(int i = 0; i<(int) Math.pow(256,3); i++)
                        {
                            reqIdList.add(-1);
                        }
                        synchronized (logr)
                        {
                            logr.notify();
                        }
                    }
                    catch (IOException e){}
                    receive();
                }
                @Override
                public void failed(Throwable exc, Object attachment)
                {
                    logr.severe("[서버 통신 안됨]");
                    if(socketChannel.isOpen()) stopClient();
                }
            });
        }
        catch (IOException e){}
    }

    void stopClient()
    {
        logr.info("[서버 연결 끊김]");
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
                    attachment.get(reqIdReceive);
                    int reqId = byteToInt(reqIdReceive);
                    attachment.position(4);
                    attachment.get(resultReceive);
                    int serverResult = byteToInt(resultReceive);
                    attachment.position(8);
                    attachment.get(listReceive);
                    attachment.position(12);
                    String leftover = new String(listReceive, StandardCharsets.UTF_8);
//                    System.out.println("willit work" + reqId+" "+ serverResult+" "+leftover);
                    Operation op = Operation.fromInteger(reqId);
                    logr.info("[requestId: "+reqId+" "+op.toString()+ " 응답받기성공]");
                    processOutput(reqId,serverResult,leftover);

                    readBuffer.clear();
                    socketChannel.read(readBuffer,readBuffer,this);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                logr.severe("[서버 통신 안됨, receive fail]");
                stopClient();
            }
        });
    }

    void send(int reqId,int reqNum , String userId,int roomNum)
    {
        writeBuffer.put(intTobyte(reqId));
        writeBuffer.position(4);
        writeBuffer.put(intTobyte(reqNum));
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
                Operation op = Operation.fromInteger(reqNum);
                logr.info("[보내기 완료 requestId: "+reqId +" "+op.toString() +" request]" );
                writeBuffer.clear();
            }

            @Override
            public void failed(Throwable exc, Object attachment)
            {
                logr.severe("[서버 통신 안됨 , send fail]");
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

    public byte[] intTobyte(int value) {
        byte[] bytes=new byte[4];
        bytes[0]=(byte)((value&0xFF000000)>>24);
        bytes[1]=(byte)((value&0x00FF0000)>>16);
        bytes[2]=(byte)((value&0x0000FF00)>>8);
        bytes[3]=(byte) (value&0x000000FF);

        return bytes;

    }
    public int byteToInt(byte[] src) {

        int newValue = 0;

        newValue |= (((int)src[0])<<24)&0xFF000000;
        newValue |= (((int)src[1])<<16)&0xFF0000;
        newValue |= (((int)src[2])<<8)&0xFF00;
        newValue |= (((int)src[3]))&0xFF;


        return newValue;
    }

    public static void main(String[] args)
    {
        ClientExample clientExample = new ClientExample();
        setupLogger();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            String input;
            while((input = br.readLine()) != null)
            {
                clientExample.processInput(input);
            }
        }
        catch(IOException e){}
    }

}
