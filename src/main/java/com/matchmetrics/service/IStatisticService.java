package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.StatisticDTO;

import java.util.List;

public interface IStatisticService {


    StatisticDTO createStatistic(StatisticDTO statisticDTO);

    List<StatisticDTO> getAllStatistics();

    StatisticDTO updateStatistic(StatisticDTO statisticDTO);

    void deleteStatistic(Long id);

}
