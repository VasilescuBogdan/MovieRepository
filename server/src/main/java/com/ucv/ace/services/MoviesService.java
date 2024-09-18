package com.ucv.ace.services;

import com.ucv.ace.types.Actor;
import com.ucv.ace.types.Movie;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
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
import java.util.*;

@Service
@Slf4j
public class MoviesService {

    private static final Map<String, String> MOVIES_IMAGES = new HashMap<>();

    private static final Map<String, String> MOVIES_RDF_TURTLES = new HashMap<>();

    public Model createRdfModelByWebURL(String url, Integer maximumMovies) throws IOException {

        Model model = ModelFactory.createDefaultModel();

        extractMoviesFromNetflixURL(fetchHtmlFromUrl(url), model, maximumMovies);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
            log.info(outputStream.toString(StandardCharsets.UTF_8));
        }

        return model;
    }

    private Document fetchHtmlFromUrl(String url) throws IOException {
        return Jsoup.connect(url)
                    .get();
    }


    private void extractMoviesFromNetflixURL(Document document, Model model, Integer maximumMovies) throws IOException {
        // Select the elements that contain movie information
        Elements movieItems = document.select("li.nm-content-horizontal-row-item");

        int movieCnt = 0;
        for (Element movieItem : movieItems) {
            if (movieCnt == maximumMovies) {
                break;
            }
            String movieId = movieItem.select(".nm-collections-link")
                                      .attr("href");
            String movieUrl = movieId.startsWith("http") ? movieId :
                                      "https://www.netflix.com" + movieId;  // Ensure full URL
            String imgSrc = movieItem.select(".nm-collections-title-img")
                                     .attr("src");
            extractMovieDetailsFromNetflixURL(movieUrl, model, imgSrc);
            movieCnt++;
        }

        // Output the RDF model in Turtle format
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
            log.info(outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    private void extractMovieDetailsFromNetflixURL(String movieUrl, Model originalModel, String imgSrc)
            throws IOException {
        Document document = fetchHtmlFromUrl(movieUrl);

        // Create a new RDF model for the current movie
        Model movieModel = ModelFactory.createDefaultModel();

        // Extract movie details
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

        MOVIES_IMAGES.put(movieName, imgSrc);

        // Generate a unique URI for the movie
        String movieUri = "http://example.com/movie/" + sanitizeUriComponent(movieName) + "_" +
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
                                           .addProperty(movieModel.createProperty("http://schema.org/url"),
                                                   movieUrl);  // Add movie URL here

        // Extract actor list
        String actorsText = document.select("span.title-data-info-item-list")
                                    .text();
        if (!actorsText.isEmpty()) {
            String[] actorNames = actorsText.split(",\\s*");
            for (String actorName : actorNames) {
                actorName = actorName.trim();  // Sanitize actor names

                // Generate a unique URI for the actor
                String actorUri = "http://example.com/person/" + sanitizeUriComponent(actorName);

                // Create actor resource and link to movie
                Resource actorResource = movieModel.createResource(actorUri)
                                                   .addProperty(RDF.type, FOAF.Person)
                                                   .addProperty(FOAF.name, actorName);
                movieResource.addProperty(movieModel.createProperty("http://schema.org/actor"), actorResource);
            }
        }

        // Convert the RDF model to Turtle format
        StringWriter out = new StringWriter();
        movieModel.write(out, "TURTLE");

        // Store the Turtle formatted RDF in the map
        MOVIES_RDF_TURTLES.put(movieName, out.toString());

        // Optionally, add movie and actor data to the original model if needed
        originalModel.add(movieModel);
    }


    // Helper method to sanitize URI components by replacing spaces and special characters
    private static String sanitizeUriComponent(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8)
                         .replace("\\+", "_");  // Replace spaces with underscores
    }


    public Collection<Movie> getMoviesWithActors(Model model, String sortingDetails, boolean isAscending,
                                                 boolean onlyThisYear, List<String> actorsThatPlay) {

        List<Movie> movies = new ArrayList<>();

        // Determine the current year
        String currentYear = String.valueOf(Year.now()
                                                .getValue());

        // Determine the SPARQL variable and sorting direction based on the inputs
        String sortBy;
        if ("year".equalsIgnoreCase(sortingDetails)) {
            sortBy = "releaseYear";
        } else {
            sortBy = "movieName";
        }

        String sortOrder = isAscending ? "ASC" : "DESC";  // Define the sort order (ASC or DESC)

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
                                actor.replaceAll("\\s", "_"),  // Avoids spaces in variable names
                                actor.replaceAll("\\s", "_"), actor));
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

            // Output the results (you can store these in a list or other structure)
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String movieName = solution.getLiteral("movieName")
                                           .getString();
                String releaseYear = solution.getLiteral("releaseYear")
                                             .getString();
                String description = solution.getLiteral("description")
                                             .getString();
                String genre = solution.getLiteral("genre")
                                       .getString();
                String actors = solution.contains("actors") ? solution.getLiteral("actors")
                                                                      .getString() : "No actors listed";
                String movieUrl = solution.getLiteral("movieUrl")
                                          .getString();  // Get the movie URL
                String image = MOVIES_IMAGES.get(movieName);

                var movie = new Movie();
                movie.setYear(Integer.parseInt(releaseYear));
                movie.setName(movieName);
                movie.setImgSrc(image);
                movie.setDescription(description);
                movie.setGenre(genre);
                movie.setUrl(movieUrl);
                movie.setRdfTurtle(MOVIES_RDF_TURTLES.get(movieName));
                movie.setActors(new ArrayList<>());

                String[] actorsNames = actors.split(", ");
                for (String name : actorsNames) {
                    movie.getActors()
                         .add(new Actor(name));
                }

                movies.add(movie);
            }
        }

        return movies;
    }


}
