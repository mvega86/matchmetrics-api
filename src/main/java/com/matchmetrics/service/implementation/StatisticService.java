package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.exception.ConflictException;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.StatisticMapper;
import com.matchmetrics.mapper.dto.StatisticDTO;
import com.matchmetrics.persistence.entity.Statistic;
import com.matchmetrics.persistence.repository.StatisticRepository;
import com.matchmetrics.service.IStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StatisticService implements IStatisticService {

    private final StatisticRepository statisticRepository;
    private final StatisticMapper statisticMapper;

    public StatisticService(StatisticRepository statisticRepository, StatisticMapper statisticMapper) {
        this.statisticRepository = statisticRepository;
        this.statisticMapper = statisticMapper;
    }

    @Override
    @Transactional
    public StatisticDTO createStatistic(StatisticDTO statisticDTO) {
        SportType sport = statisticDTO.getSportType() != null ? statisticDTO.getSportType() : SportType.FOOTBALL;
        if (statisticRepository.existsByNameAndSportType(statisticDTO.getName(), sport)) {
            throw new ConflictException("Statistic with name '" + statisticDTO.getName() + "' already exists for sport " + sport + ".");
        }

        Statistic statistic = statisticMapper.toEntity(statisticDTO);
        return statisticMapper.toDTO(statisticRepository.save(statistic));
    }

    @Override
    public List<StatisticDTO> search(String search) {
        if (search != null && search.startsWith("sport:")) {
            String sportName = search.split(":", 2)[1].trim();
            SportType sportType = SportType.valueOf(sportName.toUpperCase());
            log.info("Searching statistics by sport: {}", sportType);
            return statisticRepository.findBySportTypeOrderByNameAsc(sportType)
                    .stream()
                    .map(statisticMapper::toDTO)
                    .toList();
        }

        if (search != null && search.startsWith("name:")) {
            String name = search.split(":", 2)[1].trim();
            log.info("Searching statistics by name: {}", name);
            return statisticRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name)
                    .stream()
                    .map(statisticMapper::toDTO)
                    .toList();
        }

        log.info("Getting all statistics...");
        return statisticRepository.findAllByOrderByNameAsc()
                .stream()
                .map(statisticMapper::toDTO)
                .toList();
    }

    @Override
    public StatisticDTO updateStatistic(StatisticDTO statisticDTO) {
        // Check if the statistic exist
        log.info("Searching statisyic with id {} to update...", statisticDTO.getId());
        Optional<Statistic> optionalStatistic = statisticRepository.findById(statisticDTO.getId());
        if (optionalStatistic.isEmpty()) {
            log.error("Statistic {} not found...", statisticDTO.getName());
            throw new EntityNotFoundException("Statistic with name '" + statisticDTO.getName() + "' not exists.");
        }

        Statistic statistic = optionalStatistic.get();
        statistic.setName(statisticDTO.getName());
        statistic.setDescription(statisticDTO.getDescription());
        statistic.setUnit(statisticDTO.getUnit());
        if (statisticDTO.getSportType() != null) {
            statistic.setSportType(statisticDTO.getSportType());
        }

        return statisticMapper.toDTO(statisticRepository.save(statistic));
    }

    @Override
    public void deleteStatistic(Long id) {
        log.info("Searching statisyic with id {} to delete...", id);
        Statistic statistic = statisticRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Statistic with ID {} not found", id);
                    return new EntityNotFoundException("Statistic with ID " + id + " was not found.");

                });
        statisticRepository.deleteById(id);
        log.info("Statistic {} delete successfully.", statistic.getName());
    }
}

