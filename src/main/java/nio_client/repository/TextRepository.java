package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Text;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TextRepository
{
    private final EntityManager em;

    public void save(Text text)
    {
        em.persist(text);
    }

    public Text findByTextNum(int textNum)
    {
        try
        {
            return (Text) em.createQuery("select t from Text t where t.textNum =: textNum")
                    .setParameter("textNum", textNum)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }

    public List<Text> findAll()
    {
        return em.createQuery("select t from Text t",Text.class)
                .getResultList();
    }
}
