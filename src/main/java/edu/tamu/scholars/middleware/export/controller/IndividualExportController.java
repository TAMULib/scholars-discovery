package edu.tamu.scholars.middleware.export.controller;

import static edu.tamu.scholars.middleware.export.utility.FilenameUtility.normalizeExportFilename;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import reactor.core.publisher.Flux;

import org.springframework.context.annotation.Lazy;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import edu.tamu.scholars.middleware.discovery.assembler.model.IndividualModel;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.Organization;
import edu.tamu.scholars.middleware.discovery.model.Person;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.export.argument.ExportArg;
import edu.tamu.scholars.middleware.export.exception.UnauthorizedExportException;
import edu.tamu.scholars.middleware.export.exception.UnknownExporterTypeException;
import edu.tamu.scholars.middleware.export.service.Exporter;
import edu.tamu.scholars.middleware.export.service.ExporterRegistry;

/**
 * REST controller for exporting
 */
@RestController
public class IndividualExportController implements RepresentationModelProcessor<IndividualModel> {

    private final IndividualRepo repo;
    private final ExporterRegistry exporterRegistry;

    public IndividualExportController(@Lazy IndividualRepo repo, @Lazy ExporterRegistry exporterRegistry) {
        this.repo = repo;
        this.exporterRegistry = exporterRegistry;
    }

    @RequestMapping(value = "/individual/{id}/export", method = {
        RequestMethod.GET,
        RequestMethod.POST
    })
    public ResponseEntity<StreamingResponseBody> export(
        @PathVariable String id,
        @RequestParam(required = false, defaultValue = "people") String field,
        @RequestParam(required = false, defaultValue = "docx") String type,
        @RequestParam(required = true) String name,
        @RequestParam(required = false) List<ExportArg> export,
        @RequestBody(required = false) List<String> ids
    ) throws UnknownExporterTypeException, IllegalArgumentException {

        final Optional<Individual> individual = repo.findById(id);

        if (!individual.isPresent()) {
            throw new EntityNotFoundException(String.format("Individual with id %s not found", id));
        }

        Individual document = individual.get();
        String contentName = normalizeExportFilename(document);

        final Exporter exporter = exporterRegistry.getExporter(type);

        List<Individual> individuals;
        StreamingResponseBody responseBody;

        try {
            if (type.equals("zip")) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (Objects.isNull(authentication) || !this.isAdmin(authentication)) {
                    throw new UnauthorizedExportException("Must be administrator to use zip exporter.");
                }
            }

            if (field != null && export != null && !export.isEmpty()) {
            
                individuals = repo.getIndividualsData(id, field);

                if (individuals.isEmpty()) {
                    throw new EntityNotFoundException("No individuals found for the provided id and field");
                }

                // validate field exist on individual with id

                responseBody = exporter.streamIndividuals(Flux.fromIterable(individuals), export);

            } else if (ids != null && !ids.isEmpty()) {
                individuals = repo.findIndividualsByIds(ids);

                if (individuals.isEmpty()) {
                    throw new EntityNotFoundException("No individuals found for the provided list of ids");
                }

                // validate ids are option of export for individual id

                responseBody = exporter.streamIndividuals(individuals, name);
            } else {
                responseBody = exporter.streamIndividual(document, name);
            }

            return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, exporter.contentDisposition(contentName))
                .header(CONTENT_TYPE, exporter.contentType())
                .body(responseBody);
            
        } catch(NullPointerException npe) {
            throw new IllegalArgumentException("Request invalid", npe);
        }
    }

    @Override
    public IndividualModel process(IndividualModel resource) {
        Individual individual = resource.getContent();
        if (individual != null) {
            if (individual.getProxy().equals(Person.class.getSimpleName())) {
                addResource(resource, new ResourceLink(
                    individual,
                    "docx",
                    "",
                    "Single Page Bio",
                    "Individual single page bio export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "docx",
                    "",
                    "Profile Summary",
                    "Individual profile summary export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip", 
                    "",
                    "Last 5 Years", 
                    "Individual 5 year publications export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
                    "",
                    "Last 8 Years",
                    "Individual 8 year publications export"));
            } else if (individual.getProxy().equals(Organization.class.getSimpleName())) {
                addResource(resource, new ResourceLink(
                    individual,
                    "csv",
                    "people",
                    String.format("%s people directory", individual.getContent().get("name")),
                    "Individual collection field export"));
                addResource(resource, new ResourceLink( // has request body for selection of ids
                    individual,
                    "zip",
                    "people",
                    String.format("%s selected people profile summaries", individual.getContent().get("name")),
                    "Individual collection field profile summary export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
                    "",
                    "Last 5 Years",
                    "Organization 5 year publications export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
                    "",
                    "Last 8 Years",
                    "Organization 8 year publications export"));
            }
        }

        return resource;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN")
            );
    }

    private void addResource(IndividualModel resource, ResourceLink link) {
        try {
            resource.add(linkTo(methodOn(this.getClass()).export(
                link.getIndividual().getId(),
                link.getField(),
                link.getType(),
                link.getName(),
                new ArrayList<>(),
                new ArrayList<>()
            )).withRel(link.getName().toLowerCase().replace(" ", "_"))
                .withTitle(link.getTitle()));
        } catch (NullPointerException
            | UnknownExporterTypeException
            | IllegalArgumentException e
        ) {
            e.printStackTrace();
        }
    }

    private class ResourceLink {
        private final Individual individual;
        private final String type;
        private final String field;
        private final String name;
        private final String title;

        private ResourceLink(
            Individual individual,
            String type,
            String field,
            String name,
            String title
        ) {
            this.individual = individual;
            this.type = type;
            this.field = field;
            this.name = name;
            this.title = title;
        }

        public Individual getIndividual() {
            return individual;
        }

        public String getType() {
            return type;
        }

        public String getField() {
            return field;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }
        
    }

}
