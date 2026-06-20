package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.StatisticWayDTO;

import java.util.List;

public interface IStatisticWayService {

    List<StatisticWayDTO> getAll();
}
