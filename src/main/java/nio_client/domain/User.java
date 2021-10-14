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
public class User
{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String userName;

    @OneToMany(mappedBy = "user")
    private List<Room> userRoomList = new Vector<>();

}
