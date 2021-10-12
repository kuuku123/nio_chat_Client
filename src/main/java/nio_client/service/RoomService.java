package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Room;
import nio_client.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService
{
    private final RoomRepository roomRepository;

    public void join(Room room)
    {
        roomRepository.save(room);
    }
}
