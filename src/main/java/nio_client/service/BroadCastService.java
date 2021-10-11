package nio_client.service;

import nio_client.domain.Client;
import nio_client.domain.Room;
import nio_client.domain.Text;
import nio_client.util.MyLog;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import static nio_client.util.ElseProcess.*;
import static nio_client.util.ElseProcess.removeZero;

public class BroadCastService
{
    private final static Logger logr = MyLog.getLogr();
    private final Client client;

    public BroadCastService(Client client)
    {
        this.client = client;
    }

    public void broadcastText(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        Room sendRoom = null;
        for (Room room : client.getRoomList())
        {
            if (room.getRoomNum() == roomNum)
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

        sendRoom.addNewTextToRoom(textId,sender,chatting,notRoomRead,usefulTime);

        String toAdd = textId + " " + sender + " " + textSize + " " + chatting + " " + notRoomRead + " " +usefulTime+"\n";
        save_text(toAdd,roomNum, client.getUserId());
        if(client.getUserId().equals(sender)) return;
        if(client.getCurRoom() == null) return;
        if(client.getCurRoom().getRoomNum() != roomNum) return;
        System.out.println(sender + " : " + chatting + " " + notRoomRead + " " + usefulTime + " "+ client.getCurRoom().getRoomNum()+"번 방");
    }

    public void broadcastInvite(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        Room room = new Room(roomNum);
        boolean roomOwner = false;
        if(client.getCurRoom() != null)
        {
            for (Room room1 : client.getRoomList())
            {
                if(room1.getRoomNum() == roomNum)
                {
                    roomOwner = true;
                    break;
                }
            }
        }
        if(client.getRoomList().size() == 0)
        {
            client.setCurRoom(room);
        }
        if(!roomOwner) add_roomList(room.getRoomNum(),client.getUserId());
        client.getRoomList().add(room);
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

    public void broadcastEnter(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        for(Room r : client.getRoomList())
        {
            if(r.getRoomNum() == roomNum)
            {
                client.setCurRoom(r);
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
            for (int i = 0; i < client.getCurRoom().getTextList().size(); i++)
            {
                Text text = client.getCurRoom().getTextList().get(i);
                for(int j = start; j<=end; j++)
                {
                    if(text.getTextId() == j)
                    {
                        text.setNotReadNum(text.getNotReadNum()-1);
                        if(enterer.equals(client.getUserId()))
                        {
                            System.out.println(text.getSender() + " : " + text.getText() + " " + text.getNotReadNum()+ " "+ text.getTime() + " " +client.getCurRoom().getRoomNum()+"번 방");
                        }
                        break;
                    }
                }
            }
        }
        if(enterer == client.getUserId()) return;
        logr.info("[" + enterer + " 가 재입장 했습니다]");
    }

    public void broadcastQuitRoom(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);
        logr.info("[" + sender +" has quit room " +roomNum+ " ]");
    }

    public void broadcastFileUpload(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);

        byte[] timeReceive = new byte[12];
        leftover.get(timeReceive, 0, 12);
        String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
        String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);

        int fileNum = leftover.getInt();
        byte[] fileNameReceive = new byte[16];
        leftover.get(fileNameReceive,0,16);
        String fileName = new String(removeZero(fileNameReceive), StandardCharsets.UTF_8);

        int totalFileSize = leftover.getInt();

        System.out.println("업로더: "+sender+" 파일번호: "+fileNum+" 파일이름: "+fileName+ " 파일사이즈: "+totalFileSize+ " 가 업로드 완료 되었습니다.");
    }

    public void broadcastFileRemove(ByteBuffer leftover)
    {
        int roomNum = leftover.getInt();
        byte[] senderReceive = new byte[16];
        leftover.get(senderReceive, 0, 16);
        String sender = new String(removeZero(senderReceive), StandardCharsets.UTF_8);

        byte[] timeReceive = new byte[12];
        leftover.get(timeReceive, 0, 12);
        String time = new String(removeZero(timeReceive), StandardCharsets.UTF_8);
        String usefulTime = time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);

        int fileNum = leftover.getInt();
        byte[] fileNameReceive = new byte[16];
        leftover.get(fileNameReceive,0,16);
        String fileName = new String(removeZero(fileNameReceive), StandardCharsets.UTF_8);

        int totalFileSize = leftover.getInt();

        System.out.println("삭제요청한 사람: "+sender+" 파일번호: "+fileNum+" 파일이름: "+fileName+ " 파일사이즈: "+totalFileSize+ " 가 삭제 완료 되었습니다.");
    }
}
