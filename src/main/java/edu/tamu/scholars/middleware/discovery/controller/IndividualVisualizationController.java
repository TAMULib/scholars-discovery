package edu.tamu.scholars.middleware.discovery.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.tamu.scholars.middleware.discovery.dto.CoDataNetworkResponse;
import edu.tamu.scholars.middleware.discovery.dto.CoDataNetworkRequest;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.discovery.resource.IndividualResource;

@RepositoryRestController
public class IndividualVisualizationController implements RepresentationModelProcessor<IndividualResource> {

    @Autowired
    private IndividualRepo repo;

    @GetMapping("/individual/{id}/co-author-network")
    public ResponseEntity<CoDataNetworkResponse> coAuthorNetwork(@PathVariable String id) {
    	String dateField = "publicationDate";
    	List<String> dataFields = Arrays.asList("authors");
    	List<Pair<String, String>> typeFilters = Arrays.asList(Pair.of("class", "Document"));
    	return ResponseEntity.ok(repo.getCoDataNetwork(new CoDataNetworkRequest(id, dateField, dataFields, typeFilters)));
    }
    
    @GetMapping("/individual/{id}/co-investigator-network")
    public ResponseEntity<CoDataNetworkResponse> coInvestigatorNetwork(@PathVariable String id) {
    	String dateField = "dateTimeIntervalStart";
        List<String> dataFields = Arrays.asList("contributors", "principalInvestigators", "coPrincipalInvestigators");
        List<Pair<String, String>> typeFilters = Arrays.asList(Pair.of("class", "Relationship"), Pair.of("type", "Grant"));
    	return ResponseEntity.ok(repo.getCoDataNetwork(new CoDataNetworkRequest(id, dateField, dataFields, typeFilters)));
    }

    @Override
    public IndividualResource process(IndividualResource resource) {
        try {
            // @formatter:off
            resource.add(
                WebMvcLinkBuilder.linkTo(
                  WebMvcLinkBuilder
                    .methodOn(this.getClass())
                    .coAuthorNetwork(resource.getContent().getId())
                ).withRel("export")
            );
            resource.add(
                WebMvcLinkBuilder.linkTo(
                  WebMvcLinkBuilder
                    .methodOn(this.getClass())
                    .coInvestigatorNetwork(resource.getContent().getId())
                ).withRel("export")
            );
            // @formatter:on
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return resource;
    }

}
