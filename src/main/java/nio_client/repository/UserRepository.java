package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository
{
    private final EntityManager em;

    public void save(User user)
    {
        em.persist(user);
    }

    public User findOne(Long id)
    {
        return em.find(User.class,id);
    }

    public List<User> findByUserName(String userName)
    {
        return em.createQuery("select u from User u where u.userName =:userName", User.class)
                .setParameter("userName", userName)
                .getResultList();
    }
}
