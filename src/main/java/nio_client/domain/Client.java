package nio_client.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

@Entity
@Getter @Setter
public class Client
{
    @Id @GeneratedValue
    @Column(name="client_LongId")
    private Long id;

    private String userId;

    @OneToMany(mappedBy = "client")
    List<Room> roomList = new Vector<>();
    boolean loggedIn = false;
    boolean connection_start_fail = false;

    @OneToOne
    Room curRoom = null;
    boolean second_login = false;

    @Transient
    private AsynchronousSocketChannel socketChannel;
    @Transient
    private AsynchronousChannelGroup channelGroup;
    @Transient
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
