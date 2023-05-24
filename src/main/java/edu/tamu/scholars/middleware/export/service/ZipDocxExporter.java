package edu.tamu.scholars.middleware.export.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportFieldView;
import edu.tamu.scholars.middleware.view.model.ExportView;

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
    public StreamingResponseBody streamIndividual(Individual document, String name) {
        final List<String> type = document.getType();

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

        return outputStream -> {

            final ObjectNode node = mapper.valueToTree(document);

            Optional<ExportFieldView> multipleReference = Optional.ofNullable(exportView.get().getMultipleReference());

            List<Individual> referenceDocuments = new ArrayList<>();

            if (multipleReference.isPresent()) {
                JsonNode reference = node.get(multipleReference.get().getField());

                List<String> ids = new ArrayList<String>();
                if (reference.isArray()) {
                    ids = StreamSupport.stream(reference.spliterator(), false).map(rn -> rn.get("id").asText()).collect(Collectors.toList());
                } else {
                    ids.add(reference.get("id").asText());
                }

                referenceDocuments.addAll(fetchLazyReference(multipleReference.get(), ids));
            } else {
                referenceDocuments.add(document);
            }

            File zipFile = File.createTempFile(document.getId(), ".zip");

            try (
                FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                ZipOutputStream zos = new ZipOutputStream(outputStream)
            ) {
                for (AbstractIndexDocument refDoc : referenceDocuments) {
                    final ObjectNode refNode = mapper.valueToTree(refDoc);

                    File refDocFile = File.createTempFile(String.format("%s-", refDoc.getId()), ".docx");

                    try {
                        final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
                        final MainDocumentPart mdp = pkg.getMainDocumentPart();

                        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
                        pkg.getMainDocumentPart().addTargetPart(ndp);
                        ndp.unmarshalDefaultNumbering();

                        ObjectNode json = processDocument(refNode, exportView.get());

                        String contentHtml = handlebarsService.template(exportView.get().getContentTemplate(), json);

                        String headerHtml = handlebarsService.template(exportView.get().getHeaderTemplate(), json);

                        addMargin(mdp);

                        createAndAddHeader(pkg, headerHtml);

                        addContent(mdp, contentHtml);

                        pkg.save(refDocFile, Docx4J.FLAG_SAVE_ZIP_FILE);

                        zipFile(zos, refDocFile);

                    } catch (IOException | JAXBException | Docx4JException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private long zipFile(ZipOutputStream zos, File file) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));

        long bytesRead = 0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
        }
        zos.closeEntry();

        return bytesRead;
    }

}
