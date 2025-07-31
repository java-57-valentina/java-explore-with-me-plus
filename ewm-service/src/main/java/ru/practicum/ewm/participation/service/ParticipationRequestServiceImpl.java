package ru.practicum.ewm.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConditionNotMetException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NoAccessException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.model.ParticipationRequest.RequestStatus;
import ru.practicum.ewm.participation.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.participation.model.ParticipationRequest.RequestStatus.CANCELED;
import static ru.practicum.ewm.participation.model.ParticipationRequest.RequestStatus.CONFIRMED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final ParticipationRequestRepository requestRepo;

    /**
     * Создает запрос на участие пользователя в событии.
     *
     * @param userId  ID пользователя, который хочет подать заявку
     * @param eventId ID события, в котором хотят участвовать
     * @return DTO созданной заявки
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConditionNotMetException если заявка уже существует, или инициатор пытается участвовать в своём событии,
     *                           или событие не опубликовано, или достигнут лимит участников
     */
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Пользователь {} пытается создать запрос участия для события {}", userId, eventId);

        User user = getUserById(userId);
        Event event = getEventById(eventId);

        checkRequestNotExists(userId, eventId);
        checkNotEventInitiator(userId, event);
        checkEventIsPublished(event);
        checkParticipantLimit(event, eventId);

        RequestStatus status = determineRequestStatus(event);

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());
        request.setStatus(status);

        log.info("Создана заявка от пользователя {} на событие {} со статусом {}", userId, eventId, status);
        return ParticipationRequestMapper.toDto(requestRepo.save(request));
    }

    /**
     * Получает список всех заявок текущего пользователя.
     *
     * @param userId ID пользователя
     * @return список DTO заявок
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        return requestRepo.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEvent(Long eventId, Long initiatorId) {
        log.debug("getRequestsForEvent: {} of user: {}", eventId, initiatorId);

        User user = getUserById(initiatorId);
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(initiatorId)) {
            throw new NoAccessException("Only initiator can view requests of event");
        }

        List<ParticipationRequest> allByEventId = requestRepo.findAllByEventId(eventId);

        return allByEventId.stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    /**
     * Отменяет заявку пользователя на участие.
     *
     * @param userId    ID пользователя
     * @param requestId ID заявки на участие
     * @return DTO отменённой заявки
     * @throws NotFoundException  если заявка не найдена
     * @throws ForbiddenException если пользователь не является автором заявки
     */
    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Пользователь {} отменяет заявку с ID {}", userId, requestId);

        ParticipationRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("ParticipationRequest", requestId));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("Только автор заявки может её отменить.");
        }

        request.setStatus(CANCELED);
        return ParticipationRequestMapper.toDto(requestRepo.save(request));
    }

    // Вспомогательный метод — получаем пользователя из базы, иначе кидаем NotFoundException.
    private User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }

    // Аналогично, получаем событие из базы или кидаем исключение.
    private Event getEventById(Long eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
    }

    // Проверка: заявка уже существует? Если да — кидаем ошибку (не надо дублировать).
    private void checkRequestNotExists(Long userId, Long eventId) {
        if (requestRepo.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConditionNotMetException("Заявка на участие уже отправлена.");
        }
    }

    // Проверяем, что инициатор события не пытается подать заявку на своё событие (это нечестно).
    private void checkNotEventInitiator(Long userId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionNotMetException("Инициатор не может участвовать в своём событии.");
        }
    }

    // Проверяем, что событие опубликовано (в смысле — не в черновике и не отменено).
    private void checkEventIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionNotMetException("Нельзя участвовать в неопубликованном событии.");
        }
    }

    // Проверяем, не достигнут ли лимит участников события.
    private void checkParticipantLimit(Event event, Long eventId) {
        long confirmed = requestRepo.countByEventIdAndStatus(eventId, CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConditionNotMetException("Лимит участников события достигнут.");
        }
    }

    // Решаем, будет ли заявка сразу подтверждена или в статусе ожидания (зависит от настроек события).
    private RequestStatus determineRequestStatus(Event event) {
        return (!Boolean.TRUE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
    }
}