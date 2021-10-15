package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Text;
import nio_client.repository.TextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TextService
{
    private final TextRepository textRepository;

    public void join(Text text)
    {
        Text byTextId = textRepository.findByTextNum(text.getTextNum());
        if(byTextId == null)
        {
            textRepository.save(text);
        }
    }
}
