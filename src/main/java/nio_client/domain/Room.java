package nio_client.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Vector;

@Entity
@Getter @Setter
public class Room
{
    @Id
    private Long roomNum;

    @OneToMany(mappedBy = "room")
    private List<Text> textList = new Vector<>();

    public Room(long roomNum)
    {
        this.roomNum = roomNum;
    }

    public Room() {}

    public void addNewTextToRoom(Long textId, String sender, String text, int notRoomRead, String usefulTime,Room room)
    {
        Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime,room);
        textList.add(text1);
    }
}
