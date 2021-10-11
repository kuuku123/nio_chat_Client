package nio_client.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class Text
{
    @Id
    int textId;
    String sender;
    String text;
    int notReadNum;
    String time;

    public Text(int textId, String sender, String text, int notReadNum, String time)
    {
        this.textId = textId;
        this.sender = sender;
        this.text = text;
        this.notReadNum = notReadNum;
        this.time = time;
    }

}
