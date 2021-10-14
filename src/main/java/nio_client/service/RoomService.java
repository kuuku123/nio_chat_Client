package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Room;
import nio_client.domain.User;
import nio_client.domain.UserRoomMapping;
import nio_client.repository.RoomRepository;
import nio_client.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class RoomService
{
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public void join(Room room, String userId)
    {
        List<User> byUserName = userRepository.findByUserName(userId);
        User user = byUserName.get(0);
        Room byRoomNum = roomRepository.findByRoomNum(room.getRoomNum());
        if (byRoomNum != null)
        {
            byRoomNum.setUser(user);
            UserRoomMapping userRoomMapping = new UserRoomMapping();
            userRoomMapping.setRoom(byRoomNum);
            userRoomMapping.setUser(user);
            user.getUserRoomList().add(userRoomMapping);
        }
        else
        {
            room.setUser(user);
            UserRoomMapping userRoomMapping = new UserRoomMapping();
            userRoomMapping.setRoom(room);
            userRoomMapping.setUser(user);
            user.getUserRoomList().add(userRoomMapping);
            roomRepository.save(room);
        }
    }
}
