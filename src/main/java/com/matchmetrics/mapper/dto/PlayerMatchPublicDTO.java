package com.matchmetrics.mapper.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PlayerMatchPublicDTO {

    private Long id;
    private MatchDTO match;
    private PlayerPublicDTO player;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private String inMinuteFormatted;
    private String outMinuteFormatted;
    private Integer battingOrder;
    private String fieldPosition;
    private Boolean fieldPositionOnly;

    public static PlayerMatchPublicDTO from(PlayerMatchDTO dto) {
        PlayerMatchPublicDTO pub = new PlayerMatchPublicDTO();
        pub.setId(dto.getId());
        pub.setMatch(dto.getMatch());
        pub.setPlayer(PlayerPublicDTO.from(dto.getPlayer()));
        pub.setInTime(dto.getInTime());
        pub.setOutTime(dto.getOutTime());
        pub.setInMinuteFormatted(dto.getInMinuteFormatted());
        pub.setOutMinuteFormatted(dto.getOutMinuteFormatted());
        pub.setBattingOrder(dto.getBattingOrder());
        pub.setFieldPosition(dto.getFieldPosition());
        pub.setFieldPositionOnly(dto.getFieldPositionOnly());
        return pub;
    }
}
