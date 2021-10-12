package nio_client.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;


@Component
@Getter @Setter
public class Client
{
    private Long id;

    private String userId;

    List<Room> roomList = new Vector<>();
    boolean loggedIn = false;
    boolean connection_start_fail = false;

    Room curRoom = null;
    boolean second_login = false;

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

}
