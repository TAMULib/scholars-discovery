package edu.tamu.scholars.middleware.export.controller;

import static edu.tamu.scholars.middleware.export.utility.FilenameUtility.normalizeExportFilename;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.GetMapping;
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
import edu.tamu.scholars.middleware.export.utility.FilenameUtility;

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
        @RequestParam(required = true) String startYear,
        @RequestParam(required = true) String endYear,
        @RequestBody(required = false) List<String> ids
    ) throws UnknownExporterTypeException, IllegalArgumentException {

        List<Individual> individuals;
        String contentName;
        StreamingResponseBody responseBody;

        try {
            if (type.equals("zip")) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (Objects.isNull(authentication) || !this.isAdmin(authentication)) {
                    throw new UnauthorizedExportException("Must be administrator to use zip exporter.");
                }
            }

            Exporter exporter = exporterRegistry.getExporter(type);

            if (ids != null && !ids.isEmpty()) {
                individuals = repo.findIndividualsByIds(ids, startYear, endYear);
                if (individuals.isEmpty()) {
                    throw new EntityNotFoundException("No individuals found for the provided IDs");
                }
                contentName = name;

                responseBody = exporter.streamIndividuals(individuals, name, startYear, endYear);
            } else {
                Optional<Individual> individual = repo.findById(id, startYear, endYear);

                if (!individual.isPresent()) {
                    throw new EntityNotFoundException(String.format("Individual with id %s not found", id));
                }
                Individual document = individual.get();
                contentName = normalizeExportFilename(document);
                responseBody = exporter.streamIndividual(document, name, startYear, endYear);
            }

            return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, exporter.contentDisposition(contentName))
                .header(CONTENT_TYPE, exporter.contentType())
                .body(responseBody);
            
        } catch(NullPointerException npe) {
            throw new IllegalArgumentException("Request body for IDs is missing or invalid", npe);
        }
    }

    @GetMapping(value = "/individual/{id}/export", params = "view")
    public ResponseEntity<StreamingResponseBody> exportSection(
        @PathVariable String id,
        @RequestParam(required = false, defaultValue = "People") String view,
        @RequestParam(required = false, defaultValue = "csv") String type,
        @RequestParam(required = false) List<ExportArg> export
        ) throws UnknownExporterTypeException {
            Exporter exporter = exporterRegistry.getExporter(type);
            List<Individual> individuals = repo.getIndividualsData(id, view.toLowerCase());
            return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, exporter.contentDisposition(FilenameUtility.normalizeExportFilename(view)))
                .header(CONTENT_TYPE, exporter.contentType())
                .body(exporter.streamIndividuals(Flux.fromIterable(individuals), export));
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
                    "Individual single page bio export", null, null));
                addResource(resource, new ResourceLink(
                    individual,
                    "docx",
                    "Profile Summary",
                    "Individual profile summary export", null, null));
                addResource(resource, new ResourceLink(
                    individual,
                    "zip", 
                    "CustomYearsPublicationRange",
                    "Individual custom year publications export",
                    "startYear",
                    "endYear"));
            } else if (individual.getProxy().equals(Organization.class.getSimpleName())) {
                addResource(resource, new ResourceLink(
                    individual,
                    "zip",
                    "CustomYearsPublicationRange",
                    "Organization custom year publications export",
                    "startYear",
                    "endYear"));
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
        if (link.getIndividual() == null) {
            throw new IllegalArgumentException("Individual cannot be null");
        }
        try {
            resource.add(linkTo(methodOn(this.getClass()).export(
                link.getIndividual().getId(),
                link.getType(),
                link.getName(),
                link.getStartYear(),
                link.getEndYear(),
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
        private final String startYear;
        private final String endYear;

        private ResourceLink(
            Individual individual,
            String type,
            String name,
            String title,
            String startYear,
            String endYear
        ) {
            this.individual = individual;
            this.type = type;
            this.name = name;
            this.title = title;
            this.startYear = startYear;
            this.endYear = endYear;
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

        public String getStartYear() {
            return startYear;
        }

        public String getEndYear() {
            return endYear;
        }
        
    }

}
