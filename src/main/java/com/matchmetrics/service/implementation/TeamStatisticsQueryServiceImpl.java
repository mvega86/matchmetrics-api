package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.TeamStatisticsSummaryDTO;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.service.ITeamStatisticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamStatisticsQueryServiceImpl implements ITeamStatisticsQueryService {

    private final MatchRepository matchRepo;

    @Override
    public List<TeamStatisticsSummaryDTO> getTeamStatsList(SportType sportType, Long tournamentId) {
        List<Match> matches = tournamentId != null
                ? matchRepo.findBySportTypeAndStateAndTournamentId(sportType, MatchState.FINISHED, tournamentId)
                : matchRepo.findBySportTypeAndState(sportType, MatchState.FINISHED);

        Map<Long, TeamStatisticsSummaryDTO> byTeam = new LinkedHashMap<>();

        for (Match match : matches) {
            accumulate(byTeam, match.getHomeTeam(), match, true);
            accumulate(byTeam, match.getAwayTeam(), match, false);
        }

        List<TeamStatisticsSummaryDTO> result = new ArrayList<>(byTeam.values());
        result.sort(Comparator.comparingInt(TeamStatisticsSummaryDTO::getWins).reversed()
                              .thenComparingInt(TeamStatisticsSummaryDTO::getRunDifferential).reversed());
        return result;
    }

    private void accumulate(Map<Long, TeamStatisticsSummaryDTO> map, Team team, Match match, boolean isHome) {
        TeamStatisticsSummaryDTO dto = map.computeIfAbsent(team.getId(), id -> {
            TeamStatisticsSummaryDTO t = new TeamStatisticsSummaryDTO();
            t.setTeamId(team.getId());
            t.setTeamName(team.getName());
            t.setAcronym(team.getAcronym());
            t.setPhotoUrl(team.getPhotoUrl());
            return t;
        });

        int scored   = isHome ? match.getHomeScore() : match.getAwayScore();
        int conceded = isHome ? match.getAwayScore() : match.getHomeScore();

        dto.setGames(dto.getGames() + 1);
        dto.setRunsFor(dto.getRunsFor() + scored);
        dto.setRunsAgainst(dto.getRunsAgainst() + conceded);

        if (scored > conceded)      dto.setWins(dto.getWins() + 1);
        else if (scored < conceded) dto.setLosses(dto.getLosses() + 1);
        else                        dto.setTies(dto.getTies() + 1);

        dto.setRunDifferential(dto.getRunsFor() - dto.getRunsAgainst());
        double winPct = dto.getGames() > 0 ? (double) dto.getWins() / dto.getGames() : 0.0;
        dto.setWinPct(String.format("%.3f", winPct));
    }
}
