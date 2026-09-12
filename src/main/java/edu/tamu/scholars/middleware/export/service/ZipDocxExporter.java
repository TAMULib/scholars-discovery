package edu.tamu.scholars.middleware.export.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.export.utility.FilenameUtility;
import edu.tamu.scholars.middleware.export.utility.ZipUtility;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportFieldView;
import edu.tamu.scholars.middleware.view.model.ExportView;

/**
 * 
 */
@Service
public class ZipDocxExporter extends AbstractDocxExporter {

    private static final String TYPE = "zip";

    private static final String CONTENT_TYPE = "application/zip";

    private static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=%s.zip";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String contentDisposition(String filename) {
        return String.format(CONTENT_DISPOSITION_TEMPLATE, filename);
    }

    @Override
    public String contentType() {

        return CONTENT_TYPE;
    }

    @Override
    public StreamingResponseBody streamIndividualsByIds(List<String> ids, String name, String startYear, String endYear) {
        if ( ids.isEmpty() ) {
            throw new EntityNotFoundException("No IDs provided");
        }

        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                List<Individual> individuals = individualRepo.findIndividualsByIds(ids);
                if ( individuals.isEmpty() ) {
                    throw new EntityNotFoundException("No individuals found for the provided IDs");
                }

                for (Individual individual : individuals) {

                    processIndividualExport(individual, name, zos, startYear, endYear);
                }
            }
        };
    }

    @Override
    public StreamingResponseBody streamIndividualById(String id, String name, String startYear, String endYear) {

        Individual individual = individualRepo.getById(id);
        return outputStream -> {

            File zipFile = File.createTempFile(individual.getId(), ".zip");

            try (
                FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                ZipOutputStream zos = new ZipOutputStream(outputStream);
            ) {
                // TODO
                // processIndividualExport(individual, name, zos, startYear, endYear);
            }
        };
    }

    private void processIndividualExport(Individual individual, String name, ZipOutputStream zos, String startYear, String endYear) {

        final List<String> type = individual.getType();
        Optional<DisplayView> displayView = displayViewRepo.findByTypesIn(type);

        if (!displayView.isPresent()) {
            throw new ExportException(String.format("Could not find a display view for types: %s", String.join(", ", type)));
        }

        Optional<ExportView> exportView = displayView.get()
            .getExportViews()
            .stream()
            .filter(ev -> ev.getName().equalsIgnoreCase(name))
            .findAny();

        if (!exportView.isPresent()) {
            throw new ExportException(String.format("%s display view does not have an export view named %s", displayView.get().getName(), name));
        }

        final ObjectNode node = mapper.valueToTree(individual);
        Optional<ExportFieldView> multipleReference = Optional.ofNullable(exportView.get().getMultipleReference());

        List<AbstractIndexDocument> referenceDocuments = new ArrayList<>();
        boolean hasMultipleReference = false;

        if (multipleReference.isPresent() && !node.isNull()) {
            String fieldName = multipleReference.get().getField();
            JsonNode reference = node.path(fieldName);

            if (!reference.isMissingNode() && !reference.isNull() && !reference.isEmpty()) {
                hasMultipleReference = true;
                List<String> ids = extractIds(reference);
                referenceDocuments.addAll(fetchLazyReference(multipleReference.get(), ids, startYear, endYear));
            }
        }

        if (!hasMultipleReference) {
            if (exportView.get().getLazyReferences() != null && !node.isNull()) {
                fetchAndAttachLazyReferences(node, exportView.get().getLazyReferences(), startYear, endYear);
            }
            referenceDocuments.add(individual);
        }

        for (AbstractIndexDocument refDoc : referenceDocuments) {
            final ObjectNode refNode;

            if (hasMultipleReference) {
                refNode = mapper.valueToTree(refDoc);
                if (node.has("awardsAndHonors") && !refNode.has("awardsAndHonors")) {
                    refNode.set("awardsAndHonors", node.get("awardsAndHonors"));
                }
                if (node.has("publications") && !refNode.has("publications")) {
                    refNode.set("publications", node.get("publications"));
                }
            } else {
                refNode = node;
            }

            String filename = FilenameUtility.normalizeExportFilename(refDoc);

            try {
                File refDocFile = File.createTempFile(filename, ".docx");

                final WordprocessingMLPackage pkg = createDocx(refNode, exportView.get(), startYear, endYear);

                pkg.save(refDocFile, Docx4J.FLAG_SAVE_ZIP_FILE);
                ZipUtility.zipFile(zos, refDocFile);

            } catch (IOException | JAXBException | Docx4JException e) {
                throw new ExportException(e.getMessage());
            }
        }
    }
}
