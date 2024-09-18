package com.ucv.ace.model;

import lombok.Builder;

import java.util.List;

@Builder
public record Movie(int year, String name, String url, String imgSrc, String rdfTurtle, String description,
                    String genre, List<Actor> actors) {
}
