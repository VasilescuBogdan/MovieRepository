package com.ucv.ace.services;

import com.ucv.ace.model.Actor;
import com.ucv.ace.model.Movie;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MoviesService {

    private static final String SPACES = "\\+";
    private static final String UNDERSCORE = "_";
    private static final String COLON = ", ";
    private final Map<String, String> moviesImages = new HashMap<>();
    private final Map<String, String> moviesRdfTurtles = new HashMap<>();

    public Model createRdfModelByWebURL(String url, Integer maximumMovies) throws IOException {

        Model model = ModelFactory.createDefaultModel();

        extractMoviesFromNetflixURL(fetchHtmlFromUrl(url), model, maximumMovies);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
            log.info(outputStream.toString(StandardCharsets.UTF_8));
        }

        return model;
    }

    public List<Movie> getMoviesWithActors(Model model, String sortingDetails, boolean isAscending,
                                           boolean onlyThisYear, List<String> actorsThatPlay) {
        List<Movie> movies = new ArrayList<>();
        String currentYear = String.valueOf(Year.now()
                                                .getValue());
        String sortBy = currentYear.equalsIgnoreCase(sortingDetails) ? "releaseYear" : "movieName";
        String sortOrder = isAscending ? "ASC" : "DESC";

        // Start building the SPARQL query
        StringBuilder sparqlQueryBuilder = new StringBuilder();
        sparqlQueryBuilder.append(
                "PREFIX schema: <http://schema.org/> " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT ?movieName ?releaseYear ?description ?genre ?movieUrl (GROUP_CONCAT(DISTINCT ?actorName; " +
                "SEPARATOR=\", \") AS ?actors) " +
                // Included movieUrl
                "WHERE { " + "  ?movie a schema:Movie ; " + "         schema:name ?movieName ; " +
                "         schema:datePublished ?releaseYear ; " + "         schema:description ?description ; " +
                "         schema:genre ?genre ; " + "         schema:url ?movieUrl . " +
                // Fetch the movie URL from RDF model
                "  OPTIONAL { " + "    ?movie schema:actor ?actor . " + "    ?actor foaf:name ?actorName . " + "  } ");

        // Apply the filter for onlyThisYear
        if (onlyThisYear) {
            sparqlQueryBuilder.append(String.format("  FILTER(?releaseYear = \"%s\") ", currentYear));
        }

        // Apply the filter for actorsThatPlay if the list is not empty
        if (actorsThatPlay != null && !actorsThatPlay.isEmpty()) {
            for (String actor : actorsThatPlay) {
                sparqlQueryBuilder.append(
                        String.format("  ?movie schema:actor ?actor_%s . " +  // Ensure the actor plays in the movie
                                      "  ?actor_%s foaf:name \"%s\" . ",  // Actor name matches the provided actor
                                actor.replaceAll("\\s", UNDERSCORE), actor.replaceAll("\\s", UNDERSCORE), actor));
            }
        }

        // Sort by the given parameter
        sparqlQueryBuilder.append("} " + "GROUP BY ?movieName ?releaseYear ?description ?genre ?movieUrl ")
                          .append(
                                  // Group by movie details, including URL
                                  String.format("ORDER BY %s(?%s)", sortOrder, sortBy));

        // Convert the StringBuilder to a string
        String sparqlQueryString = sparqlQueryBuilder.toString();

        // Create a query object
        Query query = QueryFactory.create(sparqlQueryString);

        // Execute the query on the model
        try (QueryExecution execution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = execution.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                String actors = solution.contains("actors") ? solution.getLiteral("actors")
                                                                      .getString() : "No actors listed";
                String movieName = solution.getLiteral("movieName")
                                           .getString();

                Movie movie = Movie.builder()
                                   .name(movieName)
                                   .genre(solution.getLiteral("genre")
                                                  .getString())
                                   .year(Integer.parseInt(solution.getLiteral("releaseYear")
                                                                  .getString()))
                                   .description(solution.getLiteral("description")
                                                        .getString())
                                   .imgSrc(moviesImages.get(movieName))
                                   .url(solution.getLiteral("movieUrl")
                                                .getString())
                                   .rdfTurtle(moviesRdfTurtles.get(movieName))
                                   .actors(Arrays.stream(actors.split(COLON))
                                                 .map(Actor::new)
                                                 .toList())
                                   .build();
                movies.add(movie);
            }
        }

        return movies;
    }

    private void extractActorsText(Document document, Model movieModel, Resource movieResource) {
        String actorsText = document.select("span.title-data-info-item-list")
                                    .text();
        if (!actorsText.isEmpty()) {
            String[] actorNames = actorsText.split(COLON);
            for (String actorName : actorNames) {
                String actorUri = "http://example.com/person/" + sanitizeUriComponent(actorName.trim());
                Resource actorResource = movieModel.createResource(actorUri)
                                                   .addProperty(RDF.type, FOAF.Person)
                                                   .addProperty(FOAF.name, actorName);
                movieResource.addProperty(movieModel.createProperty("http://schema.org/actor"), actorResource);
            }
        }
    }

    private String sanitizeUriComponent(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8)
                         .replace(SPACES, UNDERSCORE);  // Replace spaces with underscores
    }

    private void extractMovieDetailsFromNetflixURL(String movieUrl, Model originalModel, String imgSrc)
            throws IOException {
        Document document = fetchHtmlFromUrl(movieUrl);
        Model movieModel = ModelFactory.createDefaultModel();
        String movieName = document.select(".title-title")
                                   .text();
        String description = document.select(".title-info-synopsis")
                                     .text();
        String genre = document.select(".item-genre")
                               .text();
        String releaseYear = document.select(".item-year")
                                     .text();
        String rating = document.select(".title-info-metadata-item-rating")
                                .text();

        moviesImages.put(movieName, imgSrc);

        String movieUri = "http://example.com/movie/" + sanitizeUriComponent(movieName) + UNDERSCORE +
                          System.currentTimeMillis();

        // Create movie resource and add properties
        Resource movieResource = movieModel.createResource(movieUri)
                                           .addProperty(RDF.type, movieModel.createResource("http://schema.org/Movie"))
                                           .addProperty(movieModel.createProperty("http://schema.org/name"), movieName)
                                           .addProperty(movieModel.createProperty("http://schema.org/description"),
                                                   description)
                                           .addProperty(movieModel.createProperty("http://schema.org/genre"), genre)
                                           .addProperty(movieModel.createProperty("http://schema.org/image"), imgSrc)
                                           .addProperty(movieModel.createProperty("http://schema.org/datePublished"),
                                                   releaseYear)
                                           .addProperty(movieModel.createProperty("http://schema.org/contentRating"),
                                                   rating)
                                           .addProperty(movieModel.createProperty("http://schema.org/url"), movieUrl);

        extractActorsText(document, movieModel, movieResource);

        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, movieModel, RDFFormat.TURTLE);
        moviesRdfTurtles.put(movieName, out.toString());
        originalModel.add(movieModel);
    }

    private Document fetchHtmlFromUrl(String url) throws IOException {
        return Jsoup.connect(url)
                    .get();
    }

    private void extractMoviesFromNetflixURL(Document document, Model model, Integer maximumMovies) throws IOException {
        Elements movieItems = document.select("li.nm-content-horizontal-row-item");

        int movieCnt = 0;
        for (Element movieItem : movieItems) {
            if (movieCnt == maximumMovies) {
                break;
            }
            String movieId = movieItem.select(".nm-collections-link")
                                      .attr("href");
            String movieUrl = movieId.startsWith("http") ? movieId : "https://www.netflix.com" + movieId;
            String imgSrc = movieItem.select(".nm-collections-title-img")
                                     .attr("src");
            extractMovieDetailsFromNetflixURL(movieUrl, model, imgSrc);
            movieCnt++;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
            log.info(outputStream.toString(StandardCharsets.UTF_8));
        }
    }

}
