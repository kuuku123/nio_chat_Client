package client;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientService
{
    private final static Logger logr = Logger.getGlobal();
    private Object for_startClient = new Object();
    private Object for_broadcastInvite = new Object();
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;
    ByteBuffer writeBuffer = ByteBuffer.allocate(1000);
    boolean loggedIn = false;
    boolean connection_start_fail = false;
    String userId = "not set";
    List<Integer> reqIdList = new Vector<>((int) Math.pow(256, 3));
    List<Room> roomList = new Vector<>();
    Room curRoom = null;
    boolean second_login = false;


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
        if (command.length() > 0 && command.charAt(0) == '\\') // if it is command
        {
            if (command.startsWith("login", 1))
            {
                if(command.length()<=6)
                {
                    logr.info("type your user id");
                    return;
                }
                if (loggedIn == true)
                {
                    logr.severe("already logged in");
                    return;
                }
                if(!second_login)
                {
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
                    if (connection_start_fail)
                    {
                        logr.info("[서버가 준비 안됨 기다려야함]");
                        connection_start_fail = false;
                        return;
                    }
                }
                String name = command.substring(7);
                userId = name;
                read_text_restore();
                int reqId = availableReqId(0);
                send(reqId, 0, name, -1, ByteBuffer.allocate(0));
            }
            if (loggedIn == false)
            {
                logr.info("login first");
            } else if (command.startsWith("logout", 1))
            {
                if (loggedIn == true)
                {
                    int reqId = availableReqId(1);
                    send(reqId, 1, userId, -1, ByteBuffer.allocate(0));
                }
            } else if (command.startsWith("uploadfile", 1))
            {

            } else if (command.startsWith("showfile", 1))
            {

            } else if (command.startsWith("downloadfile", 1))
            {

            } else if (command.startsWith("deletefile", 1))
            {

            } else if (command.startsWith("createroom", 1))
            {
                int reqId = availableReqId(7);
                if (loggedIn == true)
                {
                    String roomName = command.substring(12);
                    ByteBuffer roomNameBuf = ByteBuffer.allocate(16);
                    roomNameBuf.put(roomName.getBytes(StandardCharsets.UTF_8));
                    roomNameBuf.flip();
                    send(reqId, 7, userId, -1, roomNameBuf);
                }

            }
            else if (command.startsWith("quitroom", 1))
            {
                if(loggedIn == true && curRoom == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(loggedIn == true && curRoom != null)
                {
                    int i = availableReqId(8);
                    send(i,8,userId, curRoom.roomNum, ByteBuffer.allocate(0));
                    return;
                }
            } else if (command.startsWith("inviteuser", 1))
            {
                if (loggedIn == true && curRoom == null)
                {
                    logr.info("make YOUR chatroom first ");
                    return;
                } else if (loggedIn == true && curRoom != null)
                {
                    int length = command.length();
                    String query = command.substring(12);
                    String[] s1 = query.split(" ");
                    String[] s = new String[s1.length - 1];
                    for (int i = 0; i < s1.length - 1; i++)
                    {
                        s[i] = s1[i];
                    }
                    int roomNum = Integer.parseInt(s1[s1.length - 1]);
                    int userCount = s.length;
                    ByteBuffer inviteBuffer = ByteBuffer.allocate(userCount * 16 + 4);
                    inviteBuffer.putInt(userCount);
                    int i = 0;
                    for (i = 0; i < userCount; i++)
                    {
                        inviteBuffer.position(i * 16 + 4);
                        inviteBuffer.put(s[i].getBytes(StandardCharsets.UTF_8));
                    }
                    inviteBuffer.position(i * 16 + 4);
                    int a = availableReqId(9);
                    inviteBuffer.flip();
                    send(a, 9, userId, roomNum, inviteBuffer);
                }
            } else if (command.startsWith("showuser", 1))
            {

            } else if (command.startsWith("showroom", 1))
            {
                if (loggedIn == true)
                {
                    int i = availableReqId(11);
                    send(i, 11, userId, -1, ByteBuffer.allocate(0));
                }
            } else if (command.startsWith("enterroom", 1))
            {
                if (loggedIn == true)
                {
                    int i = availableReqId(12);
                    int roomNum = Integer.parseInt(command.substring(11));
                    for(Room r : roomList)
                    {
                        if (r.roomNum == roomNum)
                        {
                            curRoom = r;
                            break;
                        }
                    }
                    send(i, 12, userId, roomNum, ByteBuffer.allocate(0));
                }

            } else if (command.startsWith("enrollfile", 1))
            {


            }
            else if(command.startsWith("exitroom",1))
            {
                if(loggedIn == true && curRoom == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(loggedIn == true && curRoom != null)
                {
                    int i = availableReqId(15);
                    send(i,15,userId, curRoom.roomNum, ByteBuffer.allocate(0));
                    return;
                }
            }
            else
            {
                logr.info("no such command");
                return;
            }
        } else
        {
            if (loggedIn == false)
            {
                logr.info("login first");
                return;
            } else if (loggedIn == true && curRoom == null)
            {
                logr.info("make YOUR chatroom first ");
                return;
            } else
            {
                int i = availableReqId(2);
                ByteBuffer textBuffer = ByteBuffer.allocate(1000);
                textBuffer.put(command.getBytes(StandardCharsets.UTF_8));
                textBuffer.flip();
                send(i, 2, userId, -1, textBuffer);
            }
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
                        for (int i = 0; i < (int) Math.pow(256, 3); i++)
                        {
                            reqIdList.add(-1);
                        }
                        synchronized (for_startClient)
                        {
                            for_startClient.notify();
                        }
                    } catch (IOException e)
                    {
                    }
                    receive();
                }

                @Override
                public void failed(Throwable exc, Object attachment)
                {
                    logr.severe("[서버 통신 안됨 connect fail]");
                    connection_start_fail = true;
                    if (socketChannel.isOpen()) stopClient();
                    synchronized (for_startClient)
                    {
                        for_startClient.notify();
                    }
                }
            });
        } catch (IOException e)
        {
        } catch (NotYetConnectedException e)
        {
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    void stopClient()
    {
        logr.info("[서버 연결 끊김]");
        loggedIn = false;
        clearReqIdList();
        if (channelGroup != null && !channelGroup.isShutdown()) channelGroup.shutdown();
    }

    void receive()
    {
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        socketChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>()
        {
            @Override
            public void completed(Integer result, ByteBuffer attachment)
            {
                try
                {
                    attachment.flip();
                    int reqId = attachment.getInt();
                    attachment.position(4);
                    if (reqId == -1) processBroadcast(attachment);
                    else processOutput(reqId, attachment);

                    ByteBuffer readBuffer = ByteBuffer.allocate(1000);
                    socketChannel.read(readBuffer, readBuffer, this);
                } catch (Exception e)
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

    void send(int reqId, int reqNum, String userId, int roomNum, ByteBuffer inputData)
    {
        writeBuffer.put(intToByte(reqId));
        writeBuffer.position(4);
        writeBuffer.put(intToByte(reqNum));
        writeBuffer.position(8);
        writeBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
        writeBuffer.position(24);
        writeBuffer.put(intToByte(roomNum));
        writeBuffer.position(28);
        writeBuffer.put(inputData);
        writeBuffer.flip();
        socketChannel.write(writeBuffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                OperationEnum op = OperationEnum.fromInteger(reqNum);
                logr.info("[보내기 완료 requestId: " + reqId + " " + op.toString() + " request]");
                writeBuffer = ByteBuffer.allocate(1000);
            }

            @Override
            public void failed(Throwable exc, Object attachment)
            {
                logr.severe("[서버 통신 안됨 , send fail]");
                stopClient();
            }
        });
    }


    void processOutput(int reqId, ByteBuffer data)
    {

        byte[] resultReceive = new byte[4];
        byte[] requestNumReceive = new byte[4];
        data.get(requestNumReceive);
        int reqNum = byteToInt(requestNumReceive);
        data.get(resultReceive);
        int serverResult = byteToInt(resultReceive);
        data.position(12);
        OperationEnum op = OperationEnum.fromInteger(reqIdList.get(reqId));
        switch (op)
        {
            case login:
                loginProcess(op, reqId, serverResult, data);
                return;
            case logout:
                logoutProcess(op, reqId, serverResult);
                return;
            case sendText:
                reqIdList.set(reqId, -1);
                if (serverResult == 0)
                {
                    logr.info("text send success");
                } else if (serverResult != 0)
                {
                    logr.info("text send fail");
                }
                return;
            case fileUpload:
            case fileList:
            case fileDownload:
            case fileDelete:
            case createRoom:
                createRoomProcess(op, reqId, serverResult, data);
                return;
            case quitRoom:
                quitRoomProcess(op,reqId,serverResult,data);
                return;
            case inviteRoom:
                inviteRoomProcess(op, reqId, serverResult, data);
                return;
            case roomUserList:
            case roomList:
                roomListProcess(op, reqId, serverResult, data);
                return;
            case enterRoom:
                enterRoomProcess(op, reqId, serverResult, data);
                return;
            case enrollFile:
                return;
            case fileInfo:
                return;
            case exitRoom:
                exitRoomProcess(op,reqId,serverResult,data);
                return;
        }
    }


    void loginProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            loggedIn = true;
            logr.info("[requestId: " + reqId + " " + op + " success]");
            logr.info("[room, text info restored]");
        } else if (serverResult == 1)
        {
            logr.severe("requestId: " + reqId + " : " + op + " failed");
        }
        else if (serverResult == 4)
        {
            logr.info("requestId: " + reqId + " : " + " 중복임으로 다른 아이디입력하세요");
        }
        reqIdList.set(reqId, -1);
    }

    void logoutProcess(OperationEnum op, int reqId, int serverResult)
    {
        if (serverResult == 0)
        {
            loggedIn = false;
            second_login = true;
            curRoom = null;
            logr.info("[logout success]");
        } else if (serverResult == 1)
        {
            logr.info("logout failed");
        }
        reqIdList.set(reqId, -1);
    }

    void createRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            int roomNum = data.getInt();
            Room room = new Room(roomNum);
            curRoom = room;
            roomList.add(room);
            add_roomList(roomNum);
            logr.info("[requestId: " + reqId + " " + " roomNum: " + roomNum + " " + op + " success]");
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
    }

    void inviteRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
    }

    void roomListProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            int roomListSize = data.getInt();
            for (int i = 0; i < roomListSize; i++)
            {
                int roomNum = data.getInt();
                byte[] roomNameReceive = new byte[16];
                data.get(roomNameReceive, 0, 16);
                String roomName = new String(removeZero(roomNameReceive), StandardCharsets.UTF_8);
                int userSize = data.getInt();
                int notReadSize = data.getInt();
                System.out.println("Room Info : roomNum=" + roomNum + ", roomName=" + roomName + ", userSize=" + userSize + ", notRead=" + notReadSize);
            }
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);

    }

    void enterRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
//            int notReadCount = data.getInt();
//            for (int i = 0; i < notReadCount; i++)
//            {
//                byte[] senderReceive = new byte[16];
//                data.get(senderReceive, 0, 16);
//                String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);
//
//                byte[] timeReceive = new byte[12];
//                data.get(timeReceive,0,12);
//                String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
//                String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
//
//                int textId = data.getInt();
//                int notRoomRead = data.getInt();
//                int textSize = data.getInt();
//                byte[] textReceive = new byte[textSize];
//                data.get(textReceive, 0, textSize);
//                String text = new String(removeZero(textReceive), StandardCharsets.UTF_8);
//                Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime);
//                String toAdd = textId + " " + sender + " " + textSize + " " + text + " " + notRoomRead + "\n";

//                save_text(toAdd,roomNum);

//                System.out.println(sender + " : " + text + " " + notRoomRead + " "+ usefulTime);
//                curRoom.textList.add(text1);
//            }
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
}

    void quitRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            roomList.remove(curRoom);
            curRoom = null;

        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }

    void exitRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            curRoom = null;

        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }

    void processBroadcast(ByteBuffer leftover)
    {

        byte[] broadcastNumReceive = new byte[4];
        leftover.get(broadcastNumReceive);
        int broadcastNum = byteToInt(broadcastNumReceive);
        leftover.position(8);
        BroadcastEnum b = BroadcastEnum.fromInteger(broadcastNum);

        switch (b)
        {
            case invite_user_to_room:
                synchronized (for_broadcastInvite)
                {
                    broadcastInvite(leftover);
                }
                return;
            case quit_room:
                broadcastQuitRoom(leftover);
                return;
            case text:
                broadcastText(leftover);
                return;
            case file_upload:
                return;
            case file_remove:
                return;
            case enter_room:
                broadcastEnter(leftover);
                return;
        }
    }

    void broadcastText(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        Room sendRoom = null;
        for (Room room : roomList)
        {
            if (room.roomNum == roomNum)
            {
                sendRoom = room;
                break;
            }
        }
        leftover.position(12);
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);

        byte[] timeReceive = new byte[12];
        leftover.get(timeReceive,0,12);
        String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
        String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);

        int textId = leftover.getInt();
        int notRoomRead = leftover.getInt();
        int textSize = leftover.getInt();
        byte[] chat = new byte[1000];
        leftover.position(52);
        int position = leftover.position();
        int limit = leftover.limit();
        leftover.get(chat, 0, limit - position);
        String chatting = new String(removeZero(chat), StandardCharsets.UTF_8);
        Text text = new Text(textId, sender, chatting, notRoomRead,usefulTime);

        String toAdd = textId + " " + sender + " " + textSize + " " + chatting + " " + notRoomRead + " " +usefulTime+"\n";
        sendRoom.textList.add(text);
        save_text(toAdd,roomNum);
        if(userId.equals(sender)) return;
        if(curRoom == null) return;
        if(curRoom.roomNum != roomNum) return;
        System.out.println(sender + " : " + chatting + " " + notRoomRead + " " + usefulTime);
    }

    void broadcastInvite(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        Room room = new Room(roomNum);
        if(roomList.size() == 0)
        {
            curRoom = room;
        }
        add_roomList(room.roomNum);
        roomList.add(room);
        byte[] inviteeReceive = new byte[16];
        leftover.get(inviteeReceive, 0, 16);
        String invitee = new String(removeZero(inviteeReceive), StandardCharsets.UTF_8);
        byte[] timeReceive = new byte[12];
        leftover.get(timeReceive,0,12);
        String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
        String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
        int total_inv_count = leftover.getInt();
        List<String> inviters = new Vector<>();
        while (leftover.position() < leftover.limit())
        {
            byte[] inviterReceive = new byte[16];
            leftover.get(inviterReceive, 0, 16);
            String s = new String(removeZero(inviterReceive), StandardCharsets.UTF_8);
            inviters.add(s);
        }

        String inviterToString = "";
        for (String s : inviters)
        {
            inviterToString += " " + s;
        }

        logr.info(invitee + " has invited " + inviterToString + " to " + roomNum + " room");
    }

    void broadcastEnter(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        for(Room r : roomList)
        {
            if(r.roomNum == roomNum)
            {
                curRoom = r;
                break;
            }
        }
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String enterer = new String(removeZero(senderReceive), StandardCharsets.UTF_8);

        byte[] timeReceive = new byte[12];
        leftover.get(timeReceive, 0, 12);
        String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
        String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);

        int start = leftover.getInt();
        int end = leftover.getInt();
        if (start != -1)
        {
            for (int i = 0; i < curRoom.textList.size(); i++)
            {
                Text text = curRoom.textList.get(i);
                for(int j = start; j<=end; j++)
                {
                    if(text.textId == j)
                    {
                        text.notReadNum--;
                        if(enterer.equals(userId))
                        {
                            System.out.println(text.sender + " : " + text.text + " " + text.notReadNum+ " "+ text.time);
                        }
                        break;
                    }
                }
            }
        }
        if(enterer == userId) return;
        logr.info("[" + enterer + " 가 재입장 했습니다]");
    }

    void broadcastQuitRoom(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);
        logr.info("[" + sender +" has quit room " +roomNum+ " ]");

    }


    int availableReqId(int reqNum)
    {
        for (int i = 0; i < reqIdList.size(); i++)
        {
            if (reqIdList.get(i) == -1)
            {
                reqIdList.set(i, reqNum);
                return i;
            }
        }
        return -1;
    }

    void clearReqIdList()
    {
        for (int i = 0; i < reqIdList.size(); i++)
        {
            if (reqIdList.get(i) != -1)
            {
                reqIdList.set(i, -1);
            }
        }
    }

    public byte[] intToByte(int value)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value & 0xFF000000) >> 24);
        bytes[1] = (byte) ((value & 0x00FF0000) >> 16);
        bytes[2] = (byte) ((value & 0x0000FF00) >> 8);
        bytes[3] = (byte) (value & 0x000000FF);

        return bytes;

    }

    public int byteToInt(byte[] src)
    {

        int newValue = 0;

        newValue |= (((int) src[0]) << 24) & 0xFF000000;
        newValue |= (((int) src[1]) << 16) & 0xFF0000;
        newValue |= (((int) src[2]) << 8) & 0xFF00;
        newValue |= (((int) src[3])) & 0xFF;


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
        for (int i = 0; i < left; i++)
        {
            n[i] = reqUserId[i];
        }
        return n;
    }

    private void read_text_restore()
    {
        Path roomPath = Paths.get("./temp_db/"+userId+"/room_save.txt");
        if (Files.exists(roomPath))
        {
            try
            {
                Files.lines(roomPath).forEach(s -> {
                    String[] roomsNum = s.split(" ");
                    for(int i = 0; i<roomsNum.length; i++)
                    {
                        int roomNum = Integer.parseInt(roomsNum[i]);
                        Room room = new Room(roomNum);
                        roomList.add(room);
                        Path path = Paths.get("./temp_db/"+userId + "/text_save/" + roomNum +"_"+".txt");
                        if (Files.exists(path))
                        {
                            try
                            {
                                Files.lines(path).forEach(line ->
                                {
                                    String[] s1 = line.split(" ");
                                    int textId = Integer.parseInt(s1[0]);
                                    String sender = s1[1];
                                    int text_length = Integer.parseInt(s1[2]);
                                    String text = "";
                                    for(int j = 3; j<=s1.length-2; j++)
                                    {
                                        text += s1[j];
                                        if(j == s1.length-2) continue;
                                        text += " ";
                                    }
                                    int notRoomRead = Integer.parseInt(s1[s1.length-2]);
                                    String usefulTime = s1[s1.length-1];
                                    Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime);
                                    room.textList.add(text1);
                                });
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void save_text(String toAdd,int roomNum)
    {
        Path path = Paths.get("./temp_db/"+userId + "/text_save/" + roomNum +"_"+".txt");
        try
        {
            Files.createDirectories(path.getParent());
            Files.write(path, toAdd.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void add_roomList(int roomNum)
    {
        String s = String.valueOf(roomNum) + " ";
        Path path = Paths.get("./temp_db/"+userId+"/room_save.txt");
        try
        {
            Files.createDirectories(path.getParent());
            Files.write(path,s.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        }
        catch (IOException e){ e.printStackTrace();}
    }


    class Room
    {
        int roomNum;
        List<Text> textList = new Vector<>();

        public Room(int roomNum)
        {
            this.roomNum = roomNum;
        }
    }


    class Text
    {
        int textId;
        String sender;
        String text;
        int notReadNum;
        String time;

        public Text(int textId, String sender, String text, int notReadNum,String time)
        {
            this.textId = textId;
            this.sender = sender;
            this.text = text;
            this.notReadNum = notReadNum;
            this.time = time;
        }
    }


    public static void main(String[] args)
    {
        ClientService clientExample = new ClientService();
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
