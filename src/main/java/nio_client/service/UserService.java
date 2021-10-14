package nio_client.service;

import lombok.RequiredArgsConstructor;
import nio_client.domain.User;
import nio_client.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    public void join(User user)
    {
        userRepository.save(user);
    }

    public User findOne(Long userId)
    {
        return userRepository.findOne(userId);
    }

    public List<User> findByUserName(String userName)
    {
        return userRepository.findByUserName(userName);
    }

}
