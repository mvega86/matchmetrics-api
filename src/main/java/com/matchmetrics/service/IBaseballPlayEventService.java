package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;

import java.util.List;

public interface IBaseballPlayEventService {
    BaseballPlayEventDTO createPlayEvent(BaseballPlayEventDTO dto);
    BaseballPlayEventDTO getPlayEventById(Long id);
    List<BaseballPlayEventDTO> search(String search);
    List<BaseballPlayEventDTO> searchByTeam(Long teamId);
    BaseballPlayEventDTO updatePlayEvent(Long id, BaseballPlayEventDTO dto);
    void deletePlayEvent(Long id);
}
