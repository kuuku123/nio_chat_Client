package domain;

import service.NetworkService;
import util.SendPackage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Vector;

public class Client
{
    private String userId;
    List<Room> roomList = new Vector<>();
    boolean loggedIn = false;
    boolean connection_start_fail = false;
    Room curRoom = null;
    private SocketChannel socketChannel;
    boolean closeGroup = false;

    public Client()
    {
        try
        {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(NetworkService.selector, SelectionKey.OP_CONNECT);
            SendPackage sendPackage = new SendPackage(this, 0, 0, 0, 0, ByteBuffer.allocate(0));
            selectionKey.attach(sendPackage);
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

    public void setSocketChannel(SocketChannel socketChannel)
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

    public SocketChannel getSocketChannel()
    {
        return socketChannel;
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
