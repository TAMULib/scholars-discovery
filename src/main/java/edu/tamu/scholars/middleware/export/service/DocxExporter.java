package edu.tamu.scholars.middleware.export.service;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportView;

@Service
public class DocxExporter extends AbstractDocxExporter {

    private static final String TYPE = "docx";

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=%s.docx";

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
            try {
                final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
                final MainDocumentPart mdp = pkg.getMainDocumentPart();

                final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
                pkg.getMainDocumentPart().addTargetPart(ndp);
                ndp.unmarshalDefaultNumbering();

                ObjectNode node = mapper.valueToTree(document);

                ObjectNode json = processDocument(node, exportView.get());

                String contentHtml = handlebarsService.template(exportView.get().getContentTemplate(), json);

                String headerHtml = handlebarsService.template(exportView.get().getHeaderTemplate(), json);

                addMargin(mdp);

                createAndAddHeader(pkg, headerHtml);

                addContent(mdp, contentHtml);

                pkg.save(outputStream);

            } catch (JAXBException | Docx4JException e) {
                throw new ExportException(e.getMessage());
            }
        };
    }

}
