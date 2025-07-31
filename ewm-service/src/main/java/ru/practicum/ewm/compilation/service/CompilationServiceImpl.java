package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

/**
 * Сервис для работы с подборками событий (Compilation).
 * Здесь происходит вся магия по поиску и получению подборок из базы.
 * Если подборка не найдена — кидаем NotFoundException, чтобы не оставлять пользователя в подвешенном состоянии.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    /**
     * Получить список подборок событий с фильтрацией по признаку "закреплена" и пагинацией.
     *
     * @param pinned фильтр по закреплённости подборок (true/false), или null — без фильтра
     * @param from   количество элементов, которые нужно пропустить (для пагинации)
     * @param size   количество элементов в ответе (для пагинации)
     * @return список DTO подборок событий, может быть пустым, если подходящих нет
     */
    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = (pinned != null)
                ? compilationRepository.findByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable).getContent();

        return compilations.stream()
                .map(CompilationMapper::toDto)
                .toList();
    }

    /**
     * Получить подборку событий по её ID.
     *
     * @param compId ID подборки
     * @return DTO подборки событий
     * @throws NotFoundException если подборка с таким ID не найдена
     */
    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation", compId));
        return CompilationMapper.toDto(compilation);
    }
}