package edu.tamu.scholars.middleware.export.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.export.utility.FilenameUtility;
import edu.tamu.scholars.middleware.export.utility.ZipUtility;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportFieldView;
import edu.tamu.scholars.middleware.view.model.ExportView;
import jakarta.xml.bind.JAXBException;

/**
 * 
 */
@Service
public class ZipDocxExporter extends AbstractDocxExporter {

    private static final String TYPE = "zip";

    private static final String CONTENT_TYPE = "application/zip";

    private static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=%s.zip";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public StreamingResponseBody streamIndividuals(List<Individual> individuals, String name, String startYear, String endYear) {
        System.out.println("ZIP Ex: streamIndividuals - startYear: " + startYear + "\n endYear "+ endYear);
        return outputStream -> {

            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (Individual individual : individuals) {
                    processIndividualExport(individual, name, zos, startYear, endYear);
                }
            }
        };
    }

    @Override
    public StreamingResponseBody streamIndividual(Individual individual, String name, String startYear, String endYear) {

        return outputStream -> {

            File zipFile = File.createTempFile(individual.getId(), ".zip");
            System.out.println("\n\n streamIndividual zipFile: "+ zipFile);
            try (
                FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                ZipOutputStream zos = new ZipOutputStream(outputStream);
            ) {
                System.out.println("\n\n streamIndividual zipFile PATH: "+ zipFile.getAbsolutePath());
                processIndividualExport(individual, name, zos, startYear, endYear);
            }
        };
    }

    private void processIndividualExport(Individual individual, String name, ZipOutputStream zos, String startYear, String endYear) throws IOException {
        System.out.println("ZIP Ex: processIndividualExport - startYear: " + startYear + "\n endYear "+ endYear); 
        final List<String> type = individual.getType();
        logger.info("\n\n ZIP exporter processIndividualExport type: {}", individual.getType() );
        logger.info( "\n ZIP exporter processIndividualExport StartYear:-" + startYear + "\t ZIP exporter processIndividualExport End Year:-" + endYear);
        
        Optional<DisplayView> displayView = displayViewRepo.findByTypesIn(type);
        System.out.println("\n\n ZIP exporter processIndividualExport: displayView is Present:  " + displayView.isPresent());
        if (!displayView.isPresent()) {
            throw new ExportException(String.format("Could not find a display view for types: %s", String.join(", ", type)));
        }

        Optional<ExportView> exportView = displayView.get()
            .getExportViews()
            .stream()
            .filter(ev -> {
                System.out.println("\n\n ZIP exporter processIndividualExport ev name:" +ev.getName());
                return ev.getName().equalsIgnoreCase(name);
            })
            .findAny();

        if (!exportView.isPresent()) {
            throw new ExportException(String.format("%s display view does not have an export view named %s", displayView.get().getName(), name));
        }

        final ObjectNode node = mapper.valueToTree(individual);
        // System.out.println("\n\n ZIP exporter processIndividualExport: node:  " + mapper.writeValueAsString(node));
        Optional<ExportFieldView> multipleReference = Optional.ofNullable(exportView.get().getMultipleReference());
        // System.out.println("\n\n ZIP exporter processIndividualExport: multipleReference isPresent:  " + multipleReference.isPresent());

        List<Individual> referenceDocuments = new ArrayList<>();

        if (multipleReference.isPresent()) {
            String fieldName = multipleReference.get().getField();
            System.out.println("ZIP exporter processIndividualExport: lookup field: " + fieldName);

            JsonNode reference = node.get(fieldName);

            if ((reference == null || reference.isNull()) || node.has("publications")) {
                reference = node.get("publications");
            }

            if (reference != null && !reference.isNull() && reference.isArray() && reference.size() > 0) {
                List<String> ids = extractIds(reference);
                referenceDocuments.addAll(fetchLazyReference(multipleReference.get(), ids, startYear, endYear));
            } else {    
                System.out.println("ELSE: No references found for field '" + fieldName + "'. Exporting primary individual ID: " + individual.getId());
                referenceDocuments.add(individual);
            }
        }
        for (AbstractIndexDocument refDoc : referenceDocuments) {
            final ObjectNode refNode = mapper.valueToTree(refDoc);
            String filename = FilenameUtility.normalizeExportFilename(refDoc);
            File refDocFile = File.createTempFile(filename, ".docx");

            try {

                final WordprocessingMLPackage pkg = createDocx(refNode, exportView.get(), startYear, endYear);

                pkg.save(refDocFile, Docx4J.FLAG_SAVE_ZIP_FILE);

                ZipUtility.zipFile(zos, refDocFile);

            } catch (IOException | JAXBException | Docx4JException e) {
                throw new ExportException(e.getMessage());
            }
        }
    }

}
