package com.ucv.ace.types;

import lombok.Data;

import java.util.List;

@Data
public class Movie {

    private int year;

    private String name;

    private String url;

    private String imgSrc;

    private String rdfTurtle;

    private String description;

    private String genre;

    private List<Actor> actors;

}
