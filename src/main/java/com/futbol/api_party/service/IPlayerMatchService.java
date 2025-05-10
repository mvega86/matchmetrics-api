package com.futbol.api_party.service;

import com.futbol.api_party.mapper.dto.PlayerMatchDTO;

import java.util.List;

public interface IPlayerMatchService {
    List<PlayerMatchDTO> search(String search);
    PlayerMatchDTO save(PlayerMatchDTO playerMatchDTO);
    PlayerMatchDTO getById(Long Id);
    PlayerMatchDTO updatePlayerMatch(PlayerMatchDTO playerMatchDTO);
    void delete(Long id);

}

