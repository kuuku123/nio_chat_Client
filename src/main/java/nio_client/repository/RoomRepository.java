package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Room;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomRepository
{
    private final EntityManager em;

    public void save(Room room)
    {
        em.persist(room);
    }

    public Room findByRoomNum(long roomNum)
    {
        return em.find(Room.class,roomNum);
    }

    public List<Room> findAll()
    {
        return em.createQuery("select r from Room r", Room.class)
                .getResultList();
    }
}
