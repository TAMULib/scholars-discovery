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

    @RequestMapping(
        value = "/individual/{id}/export",
        method = {RequestMethod.GET, RequestMethod.POST}
        )
    public ResponseEntity<StreamingResponseBody> export(
        @PathVariable String id,
        @RequestParam(required = false, defaultValue = "docx") String type,
        @RequestParam(required = true) String name,
        @RequestBody(required = false) List<String> ids
    ) throws UnknownExporterTypeException, IllegalArgumentException {

        List<Individual> individuals;
        String contentName;
        StreamingResponseBody responseBody;

        if (type.equals("zip")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.isNull(authentication) || !this.isAdmin(authentication)) {
                throw new UnauthorizedExportException("Must be administrator to use zip exporter.");
            }
        }

        Exporter exporter = exporterRegistry.getExporter(type);

        if( !ids.isEmpty() ) {
            individuals = repo.findIndividualsByIds(ids);
            if (individuals.isEmpty()) {
                throw new EntityNotFoundException("No individuals found for the provided IDs");
            }
            contentName = name;
            responseBody = exporter.streamIndividuals(individuals, name);
        } else {
            Optional<Individual> individual = repo.findById(id);

            if (!individual.isPresent()) {
                throw new EntityNotFoundException(String.format("Individual with id %s not found", id));
            }
            Individual document = individual.get();
            contentName = normalizeExportFilename(document);
            responseBody = exporter.streamIndividual(document, name);
        }

        return ResponseEntity.ok()
            .header(CONTENT_DISPOSITION, exporter.contentDisposition(contentName))
            .header(CONTENT_TYPE, exporter.contentType())
            .body(responseBody);
    }

    @Override
    public IndividualModel process(IndividualModel resource) {
        Individual individual = resource.getContent();
        if (individual != null) {
            if (individual.getProxy().equals(Person.class.getSimpleName())) {
                addResource(resource, new ResourceLink(
                    individual,
                    "docx",
                    "Single Page Bio",
                    "Individual single page bio export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "docx",
                    "Profile Summary",
                    "Individual profile summary export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip", 
                    "Last 5 Years", 
                    "Individual 5 year publications export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip", 
                    "Last 8 Years", 
                    "Individual 8 year publications export"));
            } else if (individual.getProxy().equals(Organization.class.getSimpleName())) {
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
                    "Last 5 Years",
                    "Organization 5 year publications export"));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
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
                link.getType(),
                link.getName(),
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
        private final String name;
        private final String title;

        private ResourceLink(
            Individual individual,
            String type,
            String name,
            String title
        ) {
            this.individual = individual;
            this.type = type;
            this.name = name;
            this.title = title;
        }

        public Individual getIndividual() {
            return individual;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }
        
    }

}
