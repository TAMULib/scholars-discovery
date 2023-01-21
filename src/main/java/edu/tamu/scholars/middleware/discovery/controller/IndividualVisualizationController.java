package edu.tamu.scholars.middleware.discovery.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.tamu.scholars.middleware.discovery.dto.DataNetworkRequest;
import edu.tamu.scholars.middleware.discovery.dto.DataNetworkResponse;
import edu.tamu.scholars.middleware.discovery.dto.Filter;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.discovery.resource.IndividualResource;

@RepositoryRestController
public class IndividualVisualizationController implements RepresentationModelProcessor<IndividualResource> {

    @Autowired
    private IndividualRepo repo;

    @GetMapping("/individual/{id}/co-author-network")
    public ResponseEntity<DataNetworkResponse> coAuthorNetwork(@PathVariable String id) {
        String dateField = "publicationDate";
        List<String> dataFields = Arrays.asList("authors");
        List<Filter> typeFilters = Arrays.asList(Filter.of("class", "Document"));
        return ResponseEntity.ok(repo.getDataNetwork(new DataNetworkRequest(id, dateField, dataFields, typeFilters)));
    }
    
    @GetMapping("/individual/{id}/co-investigator-network")
    public ResponseEntity<DataNetworkResponse> coInvestigatorNetwork(@PathVariable String id) {
        String dateField = "dateTimeIntervalStart";
        List<String> dataFields = Arrays.asList("contributors", "principalInvestigators", "coPrincipalInvestigators");
        List<Filter> typeFilters = Arrays.asList(Filter.of("class", "Relationship"), Filter.of("type", "Grant"));
        return ResponseEntity.ok(repo.getDataNetwork(new DataNetworkRequest(id, dateField, dataFields, typeFilters)));
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
                ).withRel("co-author-network")
            );
            resource.add(
                WebMvcLinkBuilder.linkTo(
                  WebMvcLinkBuilder
                    .methodOn(this.getClass())
                    .coInvestigatorNetwork(resource.getContent().getId())
                ).withRel("co-investigator-network")
            );
            // @formatter:on
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return resource;
    }

}
