package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.PlayerMatchDTO;

import java.util.List;

public interface IPlayerMatchService {
    List<PlayerMatchDTO> search(String search);
    List<PlayerMatchDTO> searchByTeam(String search, Long teamId);
    PlayerMatchDTO save(PlayerMatchDTO playerMatchDTO);
    PlayerMatchDTO getById(Long Id);
    PlayerMatchDTO updatePlayerMatch(PlayerMatchDTO playerMatchDTO);
    void delete(Long id);

}

