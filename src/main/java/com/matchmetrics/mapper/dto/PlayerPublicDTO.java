package com.matchmetrics.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPublicDTO {
    private Long id;
    private String fullName;
    private String jerseyName;
    private Integer jerseyNumber;
    private Integer age;
    private Long teamId;
    private List<Long> teamIds;
    private String photoUrl;
    private String fieldPosition;

    public static PlayerPublicDTO from(PlayerDTO dto) {
        PlayerPublicDTO pub = new PlayerPublicDTO();
        pub.setId(dto.getId());
        pub.setFullName(dto.getFullName());
        pub.setJerseyName(dto.getJerseyName());
        pub.setJerseyNumber(dto.getJerseyNumber());
        pub.setAge(dto.getAge());
        pub.setTeamId(dto.getTeamId());
        pub.setTeamIds(dto.getTeamIds());
        pub.setPhotoUrl(dto.getPhotoUrl());
        pub.setFieldPosition(dto.getFieldPosition());
        return pub;
    }
}
