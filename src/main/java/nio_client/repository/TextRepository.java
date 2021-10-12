package nio_client.repository;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Text;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

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

}
