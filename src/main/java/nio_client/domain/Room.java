package nio_client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Vector;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Room
{
    @Id
    @Column(name = "room_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer roomNum;

    @OneToMany(mappedBy = "room")
    private List<Text> textList = new Vector<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Room(int roomNum)
    {
        this.roomNum = roomNum;
    }

    public void addNewTextToRoom(int textId, String sender, String text, int notRoomRead, String usefulTime,Room room)
    {
        Text text1 = new Text(textId, sender, text, notRoomRead,usefulTime,room);
        textList.add(text1);
    }
}
