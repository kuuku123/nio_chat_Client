package domain;

import java.util.List;
import java.util.Vector;

public class Client
{
    private String userId;
    List<Room> roomList = new Vector<>();

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public List<Room> getRoomList()
    {
        return roomList;
    }

    public void setRoomList(List<Room> roomList)
    {
        this.roomList = roomList;
    }
}
