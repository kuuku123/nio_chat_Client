package domain;

import java.util.List;
import java.util.Vector;

public class Room
{
    int roomNum;
    List<Text> textList = new Vector<>();

    public Room(int roomNum)
    {
        this.roomNum = roomNum;
    }

    public List<Text> getTextList()
    {
        return textList;
    }

    public void addNewTextToRoom(int textId, String sender, String text, int notRoomRead, String usefulTime)
    {
        Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime);
        textList.add(text1);
    }

    public int getRoomNum()
    {
        return roomNum;
    }

    public void setRoomNum(int roomNum)
    {
        this.roomNum = roomNum;
    }
}
