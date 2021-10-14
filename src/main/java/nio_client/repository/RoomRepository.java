package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Room;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

    public Room findByRoomNum(int roomNum)
    {
        try
        {
            return em.createQuery("select r from Room r where r.roomNum =:roomNum ",Room.class)
                    .setParameter("roomNum",roomNum)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }

    public List<Room> findAll()
    {
        return em.createQuery("select r from Room r", Room.class)
                .getResultList();
    }
}
