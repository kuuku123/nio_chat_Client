package domain;

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