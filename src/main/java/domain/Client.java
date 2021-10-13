package domain;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

public class Client
{
    private String userId;
    List<Room> roomList = new Vector<>();
    boolean loggedIn = false;
    boolean connection_start_fail = false;
    Room curRoom = null;
    private AsynchronousSocketChannel socketChannel;
    private AsynchronousChannelGroup channelGroup;
    boolean closeGroup = false;

    public Client()
    {
        try
        {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory());
            socketChannel = AsynchronousSocketChannel.open(channelGroup);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

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

    public boolean isCloseGroup()
    {
        return closeGroup;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel)
    {
        this.socketChannel = socketChannel;
    }

    public void setCloseGroup(boolean closeGroup)
    {
        this.closeGroup = closeGroup;
    }

    public void setRoomList(List<Room> roomList)
    {
        this.roomList = roomList;
    }

    public AsynchronousSocketChannel getSocketChannel()
    {
        return socketChannel;
    }

    public AsynchronousChannelGroup getChannelGroup()
    {
        return channelGroup;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    public boolean isConnection_start_fail()
    {
        return connection_start_fail;
    }

    public void setConnection_start_fail(boolean connection_start_fail)
    {
        this.connection_start_fail = connection_start_fail;
    }

    public Room getCurRoom()
    {
        return curRoom;
    }

    public void setCurRoom(Room curRoom)
    {
        this.curRoom = curRoom;
    }

}
