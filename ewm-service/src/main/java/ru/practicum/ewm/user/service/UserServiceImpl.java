package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users = (ids == null || ids.isEmpty())
                ? userRepository.findAll(pageable).getContent()
                : userRepository.findByIdIn(ids, pageable);
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}