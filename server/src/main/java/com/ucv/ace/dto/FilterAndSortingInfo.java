package com.ucv.ace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FilterAndSortingInfo {

    private List<String> actors;

    private String sortType;

    @JsonProperty("isAscendantOrder")
    private boolean isAscendantOrder;

    @JsonProperty("onlyNewMovies")
    private boolean onlyNewMovies;
}
