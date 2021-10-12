package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Text;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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

    public Text findByTextId(long textId)
    {
        return em.find(Text.class, textId);
    }

    public List<Text> findAll()
    {
        return em.createQuery("select t from Text t",Text.class)
                .getResultList();
    }
}
