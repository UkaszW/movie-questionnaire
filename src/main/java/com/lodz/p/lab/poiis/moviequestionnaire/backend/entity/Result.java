package com.lodz.p.lab.poiis.moviequestionnaire.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    private Long movieId;

    private Long personId;

    private Long Evaluation;

}
