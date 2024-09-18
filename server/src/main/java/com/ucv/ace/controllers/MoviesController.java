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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/movies")
@SessionScope
@Slf4j
public class MoviesController {

    private final MoviesService moviesService;

    @ModelAttribute("rdfModel")
    public Model createModel() {
        return ModelFactory.createDefaultModel();
    }

    @PostMapping("/init")
    public ResponseEntity<Boolean> init(@RequestParam String url, @RequestParam Integer maximumMovies, HttpSession session) throws IOException {
        session.setAttribute("rdfModel", moviesService.createRdfModelByWebURL(url, maximumMovies));
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PostMapping()
    public ResponseEntity<Collection<Movie>> getAllMovies(
            @RequestBody FilterAndSortingInfo filterAndSortingInfo,
            HttpSession session) {

        log.info(String.valueOf(filterAndSortingInfo));
        Model rdfModel = (Model) session.getAttribute("rdfModel");

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
