package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.StatisticLocationDTO;

import java.util.List;

public interface IStatisticLocationService {

    List<StatisticLocationDTO> getAll();
}
