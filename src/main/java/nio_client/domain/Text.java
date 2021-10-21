package nio_client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Text
{
    @Id
    @Column(name = "text_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int textNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private String sender;
    private String text;
    private int notReadNum;
    private String time;


    public Text(int textId, String sender, String text, int notReadNum, String time, Room room)
    {
        this.textNum = textId;
        this.sender = sender;
        this.text = text;
        this.notReadNum = notReadNum;
        this.time = time;
        this.room = room;
    }

}
