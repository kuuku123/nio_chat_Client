package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Text;
import nio_client.repository.TextRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;

@Service
@RequiredArgsConstructor
@Transactional
public class TextService
{
    private final TextRepository textRepository;

    public void join(Text text)
    {
        textRepository.save(text);
    }

    public Text findOne(int textNum)
    {
        Text byTextId = textRepository.findByTextNum(textNum);
        if (byTextId != null)
        {
            return byTextId;
        }
        else return null;
    }
}
