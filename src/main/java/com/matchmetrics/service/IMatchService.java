package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.MatchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IMatchService {
    MatchDTO createMatch(MatchDTO matchDTO);
    List<MatchDTO> search(String search);
    List<MatchDTO> searchByTeam(String search, Long teamId);
    Page<MatchDTO> searchPage(String search, Pageable pageable);
    MatchDTO getMatchById(Long matchId);
    MatchDTO updateMatch(MatchDTO matchDTO);
    void delete(Long id);
}

