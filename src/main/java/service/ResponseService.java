package service;

import domain.Client;
import domain.Room;
import ui.UI;
import util.ElseProcess;
import util.MyLog;
import util.OperationEnum;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import static util.ElseProcess.*;
import static util.ElseProcess.removeZero;
import static util.ElseProcess.reqIdList;

public class ResponseService
{
    private final static Logger logr = MyLog.getLogr();
    private Client client;

    public ResponseService(Client client)
    {
        this.client = client;
    }

    public void loginProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            client.setLoggedIn(true);
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

    public void logoutProcess(OperationEnum op, int reqId, int serverResult)
    {
        if (serverResult == 0)
        {
            client.setLoggedIn(false);
            client.setCurRoom(null);
            try
            {
                client.getSocketChannel().close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            logr.info("[logout success]");
        }
        else if (serverResult == 1)
        {
            logr.info("logout failed");
        }
        reqIdList.set(reqId, -1);
    }

    public void createRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            int roomNum = data.getInt();
            Room room = new Room(roomNum);
            client.setCurRoom(room);
            client.getRoomList().add(room);
            add_roomList(roomNum,client.getUserId());
            logr.info("[requestId: " + reqId + " " + " roomNum: " + roomNum + " " + op + " success]");
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
    }

    public void inviteRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
    }

    public void roomListProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
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

    public void enterRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");

        } else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId, -1);
    }

    public void quitRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            client.getRoomList().remove(client.getCurRoom());
            client.setCurRoom(null);
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }

    public void exitRoomProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if (serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            client.setCurRoom(null);
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }
    public void enrollFileProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            UI.fileNum = data.getInt();
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");

        reqIdList.set(reqId,-1);
    }

    public void fileUploadProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }

    public void roomUserListProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            String people = "";
            int size = data.getInt();
            people += "총 "+ size +" 명 ";
            System.out.println(people);
            for(int i = 0; i<size; i++)
            {
                byte[] userReceive = new byte[16];
                data.get(userReceive,0,16);
                String roomUser = new String(removeZero(userReceive), StandardCharsets.UTF_8);
                int stateNum = data.getInt();
                String state = "";
                if(stateNum == 0) state = "로그아웃";
                else if(stateNum == 1) state = "이 방에서 채팅중";
                else if(stateNum == 2) state = "이 방에서 채팅 중이지 않음";
                else if(stateNum == 3) state = "중간에 끊김";
                System.out.println(roomUser + " "+ state);
            }
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }
    public void fileDownloadProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            int fileNum = data.getInt();
            byte[] fileNameReceive = new byte[16];
            data.get(fileNameReceive,0,16);
            String fileName = new String(removeZero(fileNameReceive), StandardCharsets.UTF_8);
            int position = data.position();
            int limit = data.limit();
            int fileSize = limit - position;
            byte[] fileReceive = new byte[fileSize];
            data.get(fileReceive,0,fileSize);

            Path path = Paths.get("./temp_db/"+client.getUserId()+"/download/"+fileName);
            try
            {
                Files.createDirectories(path.getParent());
                Files.write(path,fileReceive, StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);

    }

    public void fileListProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
            int totalSize = data.getInt();
            System.out.println("총 파일 갯수 "+totalSize);
            for(int i = 0; i<totalSize; i++)
            {
                int fileNum = data.getInt();
                byte[] fileNameReceive = new byte[16];
                data.get(fileNameReceive,0,16);
                String fileName = new String(removeZero(fileNameReceive), StandardCharsets.UTF_8);
                int fileSize = data.getInt();
                System.out.println("파일번호: "+fileNum + " 파일이름: "+fileName+ " 파일사이즈: "+fileSize);
            }
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);

    }

    public void fileDeleteProcess(OperationEnum op, int reqId, int serverResult, ByteBuffer data)
    {
        if(serverResult == 0)
        {
            logr.info("[requestId: " + reqId + " " + op + " success]");
        }
        else logr.severe("requestId: " + reqId + " : " + op + " failed");
        reqIdList.set(reqId,-1);
    }

}
