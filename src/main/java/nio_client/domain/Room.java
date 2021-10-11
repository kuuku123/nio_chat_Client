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
    int roomNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_LongId")
    private Client client;

    @OneToMany
    List<Text> textList = new Vector<>();

    public Room(int roomNum)
    {
        this.roomNum = roomNum;
    }

    public Room()
    {

    }


    public void addNewTextToRoom(int textId, String sender, String text, int notRoomRead, String usefulTime)
    {
        Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime);
        textList.add(text1);
    }
}
