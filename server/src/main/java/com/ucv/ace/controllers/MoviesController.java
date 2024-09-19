package com.ucv.ace.controllers;

import com.ucv.ace.dto.FilterAndSortingInfo;
import com.ucv.ace.services.MoviesService;
import com.ucv.ace.model.Movie;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/movies")
@Slf4j
@CrossOrigin
@SessionScope
public class MoviesController {

    private final MoviesService moviesService;

    @ModelAttribute("rdfModel")
    public Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    @PostMapping()
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam int maximumMovies,
            @RequestBody FilterAndSortingInfo filterAndSortingInfo) throws IOException {

        log.info(String.valueOf(filterAndSortingInfo));
        Model rdfModel = moviesService.createRdfModelByWebURL(maximumMovies);
        if (rdfModel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(moviesService.getMoviesWithActors(
                rdfModel,
                filterAndSortingInfo.getSortType(),
                filterAndSortingInfo.isAscendantOrder(),
                filterAndSortingInfo.isOnlyNewMovies(),
                filterAndSortingInfo.getActors()));
    }
}
