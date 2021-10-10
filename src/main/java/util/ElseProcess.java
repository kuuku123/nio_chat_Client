package util;

import clientservice.ClientService;
import domain.Client;
import domain.Room;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Vector;

public class ElseProcess
{
    static List<Integer> reqIdList = new Vector<>((int) Math.pow(256, 3));
    public static int availableReqId(int reqNum)
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

    public static void clearReqIdList()
    {
        for (int i = 0; i < reqIdList.size(); i++)
        {
            if (reqIdList.get(i) != -1)
            {
                reqIdList.set(i, -1);
            }
        }
    }

//    public byte[] intToByte(int value)
//    {
//        byte[] bytes = new byte[4];
//        bytes[0] = (byte) ((value & 0xFF000000) >> 24);
//        bytes[1] = (byte) ((value & 0x00FF0000) >> 16);
//        bytes[2] = (byte) ((value & 0x0000FF00) >> 8);
//        bytes[3] = (byte) (value & 0x000000FF);
//
//        return bytes;
//
//    }
//
//    public int byteToInt(byte[] src)
//    {
//
//        int newValue = 0;
//
//        newValue |= (((int) src[0]) << 24) & 0xFF000000;
//        newValue |= (((int) src[1]) << 16) & 0xFF0000;
//        newValue |= (((int) src[2]) << 8) & 0xFF00;
//        newValue |= (((int) src[3])) & 0xFF;
//
//
//        return newValue;
//    }


    public static byte[] removeZero(byte[] reqUserId)
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

    public static void read_text_restore(Client client)
    {
        Path roomPath = Paths.get("./temp_db/"+client.getUserId()+"/room_save.txt");
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
                        client.getRoomList().add(room);
                        Path path = Paths.get("./temp_db/"+client.getUserId() + "/text_save/" + roomNum +"_"+".txt");
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
                                    room.addNewTextToRoom(textId,sender,text,notRoomRead,usefulTime);
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


    public static  void save_text(String toAdd,int roomNum, String userId)
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

    private void add_roomList(int roomNum, String userId)
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
}
