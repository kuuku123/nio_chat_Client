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

    public void addNewTextToRoom(int textId,String sender, String text, int notRoomRead, String usefulTime)
    {
        Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime);
        textList.add(text1);
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


}
