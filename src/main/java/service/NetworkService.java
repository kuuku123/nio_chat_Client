package service;

import domain.Client;
import ui.UI;
import util.BroadcastEnum;
import util.MyLog;
import util.OperationEnum;
import util.SendPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static ui.UI.for_startConnection;
import static util.ElseProcess.*;

public class NetworkService
{
    private final static Logger logr = MyLog.getLogr();
    public static Selector selector;
    private final ResponseService responseService;
    private final BroadCastService broadCastService;

    public NetworkService(ResponseService responseService, BroadCastService broadCastService)
    {
        this.responseService = responseService;
        this.broadCastService = broadCastService;
    }

    public void startConnection(Client client)
    {
        SocketChannel socketChannel = client.getSocketChannel();
        try
        {
            socketChannel.connect(new InetSocketAddress("localhost",5001));
            selector.wakeup();
        } catch (IOException e)
        {
            e.printStackTrace();
        }



        Thread thread = new Thread(() ->
        {
            while(true)
            {
                try
                {
                    int keyCount = selector.select();
                    if(keyCount == 0) continue;
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while(iterator.hasNext())
                    {
                        SelectionKey selectionKey = iterator.next();
                        if(selectionKey.isConnectable())
                        {
                            connect(selectionKey);
                        }
                        else if (selectionKey.isReadable())
                        {
                            receive(selectionKey);
                        }
                        else if(selectionKey.isWritable())
                        {
                            send(selectionKey);
                        }
                        iterator.remove();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    if(client.getSocketChannel().isOpen())
                    {
                        try
                        {
                            client.getSocketChannel().close();
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }

                    break;
                }
            }
        });
        thread.start();
    }

    void stopClient(Client client)
    {
        logr.info("[서버 연결 끊김]");
        client.setLoggedIn(false);
        clearReqIdList();
        try
        {
            client.getSocketChannel().close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void connect(SelectionKey selectionKey)
    {
        SendPackage sendPackage = (SendPackage) selectionKey.attachment();
        Client client = sendPackage.getClient();
        try
        {
            client.getSocketChannel().finishConnect();
            for (int i = 0; i < (int) Math.pow(256, 3); i++)
            {
                reqIdList.add(-1);
            }
            synchronized (for_startConnection)
            {
                for_startConnection.notify();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    void receive(SelectionKey selectionKey)
    {
        SendPackage sendPackage = (SendPackage) selectionKey.attachment();
        Client client = sendPackage.getClient();
        try
        {
            ByteBuffer readBuffer = ByteBuffer.allocate(100000);

            int byteCount = client.getSocketChannel().read(readBuffer);
            if(byteCount == -1)
            {
                return;
            }
            readBuffer.flip();
            int reqId = readBuffer.getInt();
            readBuffer.position(4);
            if (reqId == -1) processBroadcast(readBuffer);
            else processResponse(reqId, readBuffer);
            readBuffer.clear();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            stopClient(client);
        }
    }

    public void send(SelectionKey selectionKey)
    {
        SendPackage sendPackage = (SendPackage) selectionKey.attachment();
        Client client = sendPackage.getClient();
        int reqId = sendPackage.getReqId();
        int reqNum = sendPackage.getOperation();
        String userId = sendPackage.getClient().getUserId();
        int roomNum = -1;
        if(sendPackage.getClient().getCurRoom() != null)
        {
            roomNum = sendPackage.getClient().getCurRoom().getRoomNum();
        }
        ByteBuffer inputData = sendPackage.getLeftover();

        ByteBuffer writeBuffer = ByteBuffer.allocate(100000);
        writeBuffer.putInt(reqId);
        writeBuffer.position(4);
        writeBuffer.putInt(reqNum);
        writeBuffer.position(8);
        writeBuffer.put(userId.getBytes(StandardCharsets.UTF_8));
        writeBuffer.position(24);
        writeBuffer.putInt(roomNum);
        writeBuffer.position(28);
        writeBuffer.put(inputData);
        writeBuffer.flip();
        try
        {
            client.getSocketChannel().write(writeBuffer);
            selectionKey.interestOps(SelectionKey.OP_READ);
            OperationEnum op = OperationEnum.fromInteger(reqNum);
            logr.info("[보내기 완료 requestId: " + reqId + " " + op.toString() + " request]");
        } catch (IOException e)
        {
            e.printStackTrace();
            synchronized (inputData)
            {
                inputData.notify();
            }
            logr.severe("[서버 통신 안됨 , send fail]");
            stopClient(client);
        }
    }


    void processResponse(int reqId, ByteBuffer data)
    {

        int reqNum = data.getInt();
        int serverResult = data.getInt();
        data.position(12);
        OperationEnum op;
        if(reqNum == 5) op = OperationEnum.fileDownload;
        else op = OperationEnum.fromInteger(reqIdList.get(reqId));
        switch (op)
        {
            case login:
                responseService.loginProcess(op, reqId, serverResult, data);
                return;
            case logout:
                responseService.logoutProcess(op, reqId, serverResult);
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
                responseService.fileUploadProcess(op,reqId,serverResult,data);
                return;
            case fileList:
                responseService.fileListProcess(op,reqId,serverResult,data);
                return;
            case fileDownload:
                responseService.fileDownloadProcess(op,reqId,serverResult,data);
                return;
            case fileDelete:
                responseService.fileDeleteProcess(op,reqId,serverResult,data);
                return;
            case createRoom:
                responseService.createRoomProcess(op, reqId, serverResult, data);
                return;
            case quitRoom:
                responseService.quitRoomProcess(op,reqId,serverResult,data);
                return;
            case inviteRoom:
                responseService.inviteRoomProcess(op, reqId, serverResult, data);
                return;
            case roomUserList:
                responseService.roomUserListProcess(op,reqId,serverResult,data);
                return;
            case roomList:
                responseService.roomListProcess(op, reqId, serverResult, data);
                return;
            case enterRoom:
                responseService.enterRoomProcess(op, reqId, serverResult, data);
                return;
            case enrollFile:
                responseService.enrollFileProcess(op,reqId,serverResult,data);
                return;
            case fileInfo:
                return;
            case exitRoom:
                responseService.exitRoomProcess(op,reqId,serverResult,data);
                return;
        }
    }


    void processBroadcast(ByteBuffer leftover)
    {

        int broadcastNum = leftover.getInt();
        leftover.position(8);
        BroadcastEnum b = BroadcastEnum.fromInteger(broadcastNum);

        switch (b)
        {
            case invite_user_to_room:
                synchronized (UI.for_broadcastInvite)
                {
                    broadCastService.broadcastInvite(leftover);
                }
                return;
            case quit_room:
                broadCastService.broadcastQuitRoom(leftover);
                return;
            case text:
                broadCastService.broadcastText(leftover);
                return;
            case file_upload:
                broadCastService.broadcastFileUpload(leftover);
                return;
            case file_remove:
                broadCastService.broadcastFileRemove(leftover);
                return;
            case enter_room:
                broadCastService.broadcastEnter(leftover);
                return;
            case closed:
                broadCastService.broadcastClose(leftover);
        }
    }
}
