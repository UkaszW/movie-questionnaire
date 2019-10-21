package com.lodz.p.lab.poiis.moviequestionnaire.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Input {

    @Id
    private Long id;

    private Long tmdbId;

    private String title;
}
