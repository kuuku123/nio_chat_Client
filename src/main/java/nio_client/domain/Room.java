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

    @OneToMany(mappedBy = "room",cascade = CascadeType.ALL)
    private List<Text> textList = new Vector<>();

    public Room(int roomNum)
    {
        this.roomNum = roomNum;
    }

}