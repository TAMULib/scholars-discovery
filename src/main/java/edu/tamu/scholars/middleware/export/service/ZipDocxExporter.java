package edu.tamu.scholars.middleware.export.service;

import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public StreamingResponseBody streamIndividuals(List<Individual> individuals, String name) {
        return streamIndividuals(individuals, name, null, null);

        // return outputStream -> {

        //     try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
        //         for (Individual individual : individuals) {
        //             processIndividualExport(individual, name, zos);
        //         }
        //     }
        // };
    }

    // Duplicate - to handle the custom year range filtering for publications
    public StreamingResponseBody streamIndividuals(List<Individual> individuals, String name, String startYear, String endYear) {

        boolean hastartYearStringFilter = startYear != null && endYear != null;

        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (Individual individual : individuals) {
                    if (hastartYearStringFilter) {
                        processIndividualExportWithYearFilter(individual, name, zos, startYear, endYear);
                    } else {
                        processIndividualExportWithYearFilter(individual, name, zos, null, null);
                    }
                }
            }
        };
    }

    @Override
    public StreamingResponseBody streamIndividual(Individual individual, String name) {

        return outputStream -> {

            File zipFile = File.createTempFile(individual.getId(), ".zip");

            try (
                FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                ZipOutputStream zos = new ZipOutputStream(outputStream);
            ) {
                processIndividualExport(individual, name, zos);
            }
        };
    }

    @Override
    public StreamingResponseBody streamIndividual(Individual individual, String name, String startYear, String endYear) {

        return outputStream -> {

            File zipFile = File.createTempFile(individual.getId(), ".zip");

            try (
                FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                ZipOutputStream zos = new ZipOutputStream(outputStream);
            ) {
                processIndividualExportWithYearFilter(individual, name, zos, startYear, endYear);
            }
        };
    }

    private void processIndividualExport(Individual individual, String name, ZipOutputStream zos) throws IOException {
        processIndividualExportWithYearFilter(individual, name, zos, null, null);
    }

    private void processIndividualExportWithYearFilter(Individual individual, String name, ZipOutputStream zos, String startYear, String endYear) throws IOException {
        final List<String> type = individual.getType();
        DisplayView displayView = displayViewRepo.findByTypesIn(type)
            .orElseThrow(() -> new ExportException(String.format("Could not find a display view for types: %s", String.join(", ", type))));

        ExportView exportView = displayView.getExportViews()
            .stream()
            .filter(ev -> ev.getName().equalsIgnoreCase(name))
            .findAny()
            .orElseThrow(() -> new ExportException(String.format("%s display view does not have an export view named %s", displayView.getName(), name)));

        final ObjectNode node = mapper.valueToTree(individual);
        List<AbstractIndexDocument> referenceDocuments = new ArrayList<>();

        List<ExportFieldView> lazyReferences = exportView.getLazyReferences();
        if ((lazyReferences == null || lazyReferences.isEmpty()) && exportView.getMultipleReference() != null) {
            lazyReferences = List.of(exportView.getMultipleReference());
        }

        if (lazyReferences != null && !lazyReferences.isEmpty()) {
            ExportFieldView targetReference = lazyReferences.stream()
                .filter(ref -> "publications".equalsIgnoreCase(ref.getField()))
                .findFirst()
                .orElse(lazyReferences.get(0));

            String fieldName = targetReference.getField();
            System.out.println("\n\n\n fieldname: " + fieldName);
            JsonNode reference = node.has(fieldName) ? node.get(fieldName) : findJsonNodeRecursive(node, fieldName);
            System.out.println("\n\n\n reference: " + reference);
            if (reference != null && !reference.isNull() && !reference.isMissingNode() && reference.isArray() && reference.size() > 0) {
                List<String> ids = extractIds(reference);
                if (ids != null && !ids.isEmpty()) {
                    boolean hastartYearStringFilter = startYear != null && !startYear.isBlank() && endYear != null && !endYear.isBlank();
                    if (hastartYearStringFilter) {
                        referenceDocuments.addAll(fetchLazyReferenceWithYearFilter(targetReference, ids, startYear, endYear));
                    } else {
                        referenceDocuments.addAll(fetchLazyReference(targetReference, ids));
                    }
                }
            }
        }

        if (referenceDocuments.isEmpty()) {
            referenceDocuments.add(individual);
        }

        for (AbstractIndexDocument refDoc : referenceDocuments) {
            final ObjectNode refNode = mapper.valueToTree(refDoc);
            String filename = FilenameUtility.normalizeExportFilename(refDoc);
            File refDocFile = File.createTempFile(filename, ".docx");

            try {
                final WordprocessingMLPackage pkg = createDocx(refNode, exportView);
                pkg.save(refDocFile, Docx4J.FLAG_SAVE_ZIP_FILE);
                ZipUtility.zipFile(zos, refDocFile);
            } catch (IOException | JAXBException | Docx4JException e) {
                throw new ExportException(e.getMessage());
            }
        }
    }

    private JsonNode findJsonNodeRecursive(JsonNode currentNode, String targetField) {
        System.out.println("\n\n\n ZipDocxExporter:findJsonNodeRecursive: searching for fieldName: " + targetField + "\n\n\n");

        if (currentNode == null || currentNode.isMissingNode() || currentNode.isNull()) {
            return null;
        }

        List<String> targetFields = new java.util.ArrayList<>();
        targetFields.add(targetField);
        if (targetField.equalsIgnoreCase("publications") || targetField.equalsIgnoreCase("selectedPublications") || targetField.equalsIgnoreCase("people")) {
            targetFields.add("creativeWorks");
            targetFields.add("authors");
            targetFields.add("contributors");
            targetFields.add("creators");
        }

        if (currentNode.isObject()) {
            for (String field : targetFields) {
                if (currentNode.has(field)) {
                    JsonNode val = currentNode.get(field);
                    if (isValidReferenceNode(val)) {
                        return val;
                    }
                }
            }

            var fields = currentNode.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                boolean matches = targetFields.stream().anyMatch(tf ->
                    key.equalsIgnoreCase(tf) || key.toLowerCase().contains(tf.toLowerCase())
                );

                if (matches && isValidReferenceNode(value)) {
                    return value;
                }

                if (value != null && (value.isObject() || value.isArray())) {
                    JsonNode found = findJsonNodeRecursive(value, targetField);
                    if (found != null && isValidReferenceNode(found)) {
                        return found;
                    }
                }
            }
        } else if (currentNode.isArray()) {
            for (JsonNode element : currentNode) {
                JsonNode found = findJsonNodeRecursive(element, targetField);
                if (found != null && isValidReferenceNode(found)) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean isValidReferenceNode(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return false;
        }
        if (node.isBoolean()) {
            return false;
        }
        if (node.isTextual()) {
            String text = node.asText().trim().toLowerCase();
            if (text.equals("true") || text.equals("false")) {
                return false;
            }
        }
        return node.isArray() || node.isObject() || (node.isTextual() && node.asText().contains("::"));
    }
}
