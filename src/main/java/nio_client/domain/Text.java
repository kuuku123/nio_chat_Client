package nio_client.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter @Setter
public class Text
{
    @Id
    private Long textId;

    @ManyToOne
    @JoinColumn(name = "roomNum")
    private Room room;
    private String sender;
    private String text;
    private long notReadNum;
    private String time;

    public Text(Long textId, String sender, String text, int notReadNum, String time, Room room)
    {
        this.textId = textId;
        this.sender = sender;
        this.text = text;
        this.notReadNum = notReadNum;
        this.time = time;
        this.room = room;
    }

}
