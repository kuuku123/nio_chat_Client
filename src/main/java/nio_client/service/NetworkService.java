package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Client;
import nio_client.domain.Room;
import nio_client.ui.UI;
import nio_client.util.BroadcastEnum;
import nio_client.util.MyLog;
import nio_client.util.OperationEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetConnectedException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static nio_client.ui.UI.for_startConnection;
import static nio_client.util.ElseProcess.*;

@Service
@RequiredArgsConstructor
public class NetworkService
{
    private final static Logger logr = MyLog.getLogr();
    private final ResponseService responseService;
    private final BroadCastService broadCastService;
    private final RoomService roomService;

    @Transactional
    public void startConnection(Client client)
    {
        try
        {

            client.getSocketChannel().connect(new InetSocketAddress("localhost", 5001), null, new CompletionHandler<Void, Object>()
            {
                @Override
                public void completed(Void result, Object attachment)
                {

                    try
                    {
                        logr.info("[연결완료: " + client.getSocketChannel().getRemoteAddress() + "]");
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
                    receive(client);
                }

                @Override
                public void failed(Throwable exc, Object attachment)
                {
                    logr.severe("[서버 통신 안됨 connect fail]");
                    client.setConnection_start_fail(true);
                    if (client.getSocketChannel().isOpen()) stopClient(client);
                    synchronized (for_startConnection)
                    {
                        for_startConnection.notify();
                    }
                }
            });
        } catch (NotYetConnectedException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    void stopClient(Client client)
    {
        logr.info("[서버 연결 끊김]");
        client.setLoggedIn(false);
        clearReqIdList();
        if (client.isCloseGroup() && client.getChannelGroup() != null && !client.getChannelGroup().isShutdown()) client.getChannelGroup().shutdown();
    }

    void receive(Client client)
    {
        ByteBuffer readBuffer = ByteBuffer.allocate(10000);
        client.getSocketChannel().read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>()
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
                    else processResponse(reqId, attachment);

                    ByteBuffer readBuffer = ByteBuffer.allocate(10000);
                    client.getSocketChannel().read(readBuffer, readBuffer, this);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                logr.severe("[서버 통신 안됨, receive fail]");
                stopClient(client);
            }
        });
    }

    public void send(int reqId, int reqNum, String userId, int roomNum, ByteBuffer inputData , Client client)
    {
        ByteBuffer writeBuffer = ByteBuffer.allocate(10000);
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
        client.getSocketChannel().write(writeBuffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                OperationEnum op = OperationEnum.fromInteger(reqNum);
                logr.info("[보내기 완료 requestId: " + reqId + " " + op.toString() + " request]");
            }

            @Override
            public void failed(Throwable exc, Object attachment)
            {
                logr.severe("[서버 통신 안됨 , send fail]");
                stopClient(client);
            }
        });
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
        }
    }







}
