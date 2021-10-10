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

    public class Text
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

        public int getTextId()
        {
            return textId;
        }

        public void setTextId(int textId)
        {
            this.textId = textId;
        }

        public String getSender()
        {
            return sender;
        }

        public void setSender(String sender)
        {
            this.sender = sender;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public int getNotReadNum()
        {
            return notReadNum;
        }

        public void setNotReadNum(int notReadNum)
        {
            this.notReadNum = notReadNum;
        }

        public String getTime()
        {
            return time;
        }

        public void setTime(String time)
        {
            this.time = time;
        }
    }


}
