import util.BroadcastEnum;
import util.LogFormatter;
import util.OperationEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetConnectedException;
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
    private Object for_startClient = new Object();
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;
    ByteBuffer readBuffer = ByteBuffer.allocate(1000);
    ByteBuffer writeBuffer = ByteBuffer.allocate(1000);
    boolean loggedIn = false;
    boolean connection_start_fail = false;
    String userId = "not set";
    List<Integer> reqIdList = new Vector<>((int) Math.pow(256,3));
    List<Integer> roomList = new Vector<>();
    int curRoom = -1;


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
        if(command.length()>0 && command.charAt(0) == '\\') // if it is command
        {
            if(command.startsWith("login", 1))
            {
                if (loggedIn == true)
                {
                    logr.severe("already logged in");
                    return;
                }
                synchronized (for_startClient)
                {
                    try
                    {
                        startClient();
                        for_startClient.wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                if(connection_start_fail)
                {
                    logr.info("[서버가 준비 안됨 기다려야함]");
                    connection_start_fail = false;
                    return;
                }
                String name = command.substring(7);
                userId = name;
                int reqId = availableReqId(0);
                send(reqId,0,name,-1,"");

            }
            else if(command.startsWith("logout", 1))
            {
                if (loggedIn == true)
                {
                    int reqId = availableReqId(1);
                    send(reqId,1,userId,-1,"");
                }
                else if (loggedIn == false)
                {
                    logr.info("로그인 아직 안함, 로그인먼저");
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
                int reqId = availableReqId(7);
                if(loggedIn == false)
                {
                    logr.info("login first");
                    return;
                }
                else if(loggedIn == true)
                {
                    String roomName = command.substring(12);
                    send(reqId,7,userId,-1,roomName);
                }

            }
            else if(command.startsWith("exitroom", 1))
            {

            }
            else if(command.startsWith("inviteuser", 1))
            {
                if(loggedIn == false)
                {
                    logr.info("login first");
                    return;
                }
                else if(loggedIn == true && curRoom == -1)
                {
                    logr.info("make YOUR chatroom first ");
                    return;
                }
                else if(loggedIn == true && curRoom != -1)
                {
                    String userList = command.substring(12);
                    int i = availableReqId(9);
                    send(i,9,userId,curRoom,userList);
                }
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
            else
            {
                logr.info("no such command");
                return;
            }
        }
        else
        {
            if(loggedIn==false)
            {
                logr.info("login first");
                return;
            }
            else if(loggedIn == true && curRoom == -1)
            {
                logr.info("make YOUR chatroom first ");
                return;
            }
            else
            {
                int i = availableReqId(2);
                send(i,2,userId,-1,command);
            }
        }
    }

    void processOutput(int reqId, int serverResult, ByteBuffer data)
    {
            OperationEnum op = OperationEnum.fromInteger(reqIdList.get(reqId));
            switch (op)
            {
                case login:
                    loginProcess(op,reqId,serverResult);
                    return;
                case logout:
                    logoutProcess(op,reqId,serverResult);
                    return;
                case sendText:
                    reqIdList.set(reqId,-1);
                    if (serverResult == 0)
                    {
                        logr.info("text send success");
                    }
                    else if(serverResult != 0)
                    {
                        logr.info("text send fail");
                    }
                    return;
                case fileUpload:
                case fileList:
                case fileDownload:
                case fileDelete:
                case createRoom:
                    createRoomProcess(op,reqId,serverResult,data);
                    return;
                case quitRoom:
                case inviteRoom:
                    inviteRoomProcess(op,reqId,serverResult,data);
                case roomUserList:
            }
    }

    void loginProcess(OperationEnum op, int reqId, int serverResult)
    {
        if (serverResult == 0)
        {
            loggedIn = true;
            logr.info(op.toString()+" 성공함");
            logr.info("[requestId: "+reqId+" "+op+ "success]");
        }
        else if (serverResult == 1)
        {
            logr.severe("requestId: "+reqId+" : " +op +" failed");
        }
        else if (serverResult == 4)
        {
            logr.info("requestId: "+reqId+" : " + " 중복임으로 다른 아이디입력하세요");
        }
        reqIdList.set(reqId,-1);
    }

    void logoutProcess(OperationEnum op, int reqId, int serverResult)
    {
        if(serverResult == 0)
        {
            try
            {
                socketChannel.close();
                userId = "not set";
                loggedIn = false;
                logr.info("[서버와 연결종료]");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(serverResult == 1)
        {
            logr.info("logout failed");
        }
        reqIdList.set(reqId,-1);
    }

    void createRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            int roomNum = data.getInt(8);
            curRoom = roomNum;
            roomList.add(roomNum);
            logr.info("[requestId: "+reqId+" "+op+ " success]");
        }
        else
        {
            logr.severe("requestId: "+reqId+" : " +op +" failed");
        }
        reqIdList.set(reqId,-1);
    }

    void inviteRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: "+reqId+" "+op+ " success]");
        }
        else
        {
            logr.severe("requestId: "+reqId+" : " +op +" failed");
        }
        reqIdList.set(reqId,-1);
    }




    void processBroadcast(int broadcastNum, String data, ByteBuffer leftover)
    {
        BroadcastEnum b = BroadcastEnum.fromInteger(broadcastNum);

        switch (b)
        {
            case invite_user_to_room:
                broadcastInvite(data,leftover);
                return;
            case quit_room:
                return;
            case text:
                broadcastText(data,leftover);
                return;
            case file_upload:
                return;
            case file_remove:
                return;
        }
    }

    void broadcastText(String sender, ByteBuffer leftover)
    {
        byte[] chat = new byte[1000];
        int position = leftover.position();
        int limit = leftover.limit();
        leftover.get(chat,0,limit-position);
        String chatting = new String(removeZero(chat), StandardCharsets.UTF_8);
        System.out.println(sender +" : "+chatting);
    }

    void broadcastInvite(String sender, ByteBuffer leftover)
    {
        int roomNum = leftover.getInt(24);
        System.out.println(roomNum + "방번호임");
        curRoom = roomNum;
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
                        synchronized (for_startClient)
                        {
                            for_startClient.notify();
                        }
                    }
                    catch (IOException e){}
                    receive();
                }
                @Override
                public void failed(Throwable exc, Object attachment)
                {
                    logr.severe("[서버 통신 안됨 connect fail]");
                    connection_start_fail = true;
                    if(socketChannel.isOpen()) stopClient();
                    synchronized (for_startClient)
                    {
                        for_startClient.notify();
                    }
                }
            });
        }
        catch (IOException e)
        {}
        catch (NotYetConnectedException e)
        {
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    void stopClient()
    {
        logr.info("[서버 연결 끊김]");
        loggedIn = false;
        clearReqIdList();
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
                    attachment.get(reqIdReceive);
                    int reqId = byteToInt(reqIdReceive);
                    attachment.position(4);
                    if (reqId == -1)
                    {
                        byte[] broadcastNumReceive = new byte[4];
                        byte[] userIdReceive = new byte[16];
                        byte[] leftover = new byte[1000];
                        attachment.get(broadcastNumReceive);
                        int broadcastNum = byteToInt(broadcastNumReceive);
                        attachment.position(8);
                        attachment.get(userIdReceive);
                        String info = new String(removeZero(userIdReceive), StandardCharsets.UTF_8);
                        attachment.position(24);

                        processBroadcast(broadcastNum,info,attachment);
                    }
                    else
                    {
                        byte[] resultReceive = new byte[4];
                        attachment.get(resultReceive);
                        int serverResult = byteToInt(resultReceive);
                        attachment.position(8);
//                    System.out.println("willit work" + reqId+" "+ serverResult+" "+leftover);
                        processOutput(reqId,serverResult,attachment);
                    }
                    readBuffer = ByteBuffer.allocate(1000);
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

    void send(int reqId,int reqNum , String userId,int roomNum,String inputData)
    {
        writeBuffer.put(intTobyte(reqId));
        writeBuffer.position(4);
        writeBuffer.put(intTobyte(reqNum));
        writeBuffer.position(8);
        writeBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
        writeBuffer.position(24);
        writeBuffer.put((byte) roomNum);
        writeBuffer.position(28);
        writeBuffer.put(inputData.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();
        socketChannel.write(writeBuffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                OperationEnum op = OperationEnum.fromInteger(reqNum);
                logr.info("[보내기 완료 requestId: "+reqId +" "+op.toString() +" request]" );
                writeBuffer = ByteBuffer.allocate(1000);
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
                reqIdList.set(i,reqNum);
                return i;
            }
        }
        return -1;
    }

    void clearReqIdList()
    {
        for (int i = 0; i<reqIdList.size(); i++)
        {
            if(reqIdList.get(i) != -1)
            {
                reqIdList.set(i,-1);
            }
        }
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


    private byte[] removeZero(byte[] reqUserId)
    {
        int count = 0;
        for (byte b : reqUserId)
        {
            if (b == (byte) 0) count++;
        }
        int left = reqUserId.length - count;
        byte[] n = new byte[left];
        for (int i = 0; i<left; i++)
        {
            n[i] = reqUserId[i];
        }
        return n;
    }

    public static void main(String[] args)
    {
        ClientExample clientExample = new ClientExample();
        setupLogger();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            System.out.println("입력 가능...");
            String input;
            while((input = br.readLine()) != null)
            {
                clientExample.processInput(input);
            }
        }
        catch(IOException e){}
    }

}
