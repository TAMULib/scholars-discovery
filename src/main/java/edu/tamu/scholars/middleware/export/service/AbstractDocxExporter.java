package edu.tamu.scholars.middleware.export.service;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletContext;
import jakarta.xml.bind.JAXBException;

import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.util.UriComponents;

import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.service.TemplateService;
import edu.tamu.scholars.middleware.view.model.ExportFieldView;
import edu.tamu.scholars.middleware.view.model.ExportView;
import edu.tamu.scholars.middleware.view.model.repo.DisplayViewRepo;

/**
 * 
 */
public abstract class AbstractDocxExporter implements Exporter {

    private static final ContentType HTML_CONTENT_TYPE = new ContentType("text/html");

    private static final ObjectFactory WML_OBJECT_FACTORY = Context.getWmlObjectFactory();

    @Autowired
    protected DisplayViewRepo displayViewRepo;

    @Autowired
    protected IndividualRepo individualRepo;

    @Autowired
    protected TemplateService handlebarsService;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    private ServletContext context;

    @Value("${vivo.base-url:http://localhost:8080/vivo}")
    protected String vivoUrl;

    @Value("${ui.url:http://localhost:4200}")
    protected String uiUrl;

    protected WordprocessingMLPackage createDocx(
        ObjectNode node,
        ExportView exportView,
        String startYear,
        String endYear
    ) throws JAXBException, Docx4JException {
        final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        final MainDocumentPart mdp = pkg.getMainDocumentPart();

        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        pkg.getMainDocumentPart().addTargetPart(ndp);
        ndp.unmarshalDefaultNumbering();

        ObjectNode json = processDocument(node, exportView, startYear, endYear);

        String contentHtml;
        String headerHtml;

        try {
            System.out.println("JSON Payload: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));

            contentHtml = exportView.getContentTemplate() != null
                ? handlebarsService.template(exportView.getContentTemplate(), json)
                : "";
            headerHtml = exportView.getHeaderTemplate() != null
                ? handlebarsService.template(exportView.getHeaderTemplate(), json)
                : "";
        } catch (Exception e) {
            System.out.println("\n\n FAILED ON INDIVIDUAL ID: " + (json.has("id") ? json.get("id").asText() : "UNKNOWN"));
            throw new ExportException(String.format("Failed to render export template for view '%s': %s",
                exportView.getName(), e.getMessage()));
        }

        addMargin(mdp);

        if (headerHtml != null && !headerHtml.isEmpty()) {
            createAndAddHeader(pkg, headerHtml);
        }

        if (contentHtml != null && !contentHtml.isEmpty()) {
            addContent(mdp, contentHtml);
        }

        return pkg;
    }

    protected ObjectNode processDocument(final ObjectNode node, ExportView view, String startYear, String endYear) {
        if (node == null) {
            return mapper.createObjectNode();
        }

        final UriComponents uriComponents = fromCurrentRequest()
            .replacePath(context.getContextPath())
            .replaceQuery(null)
            .build();

        node.put("serviceUrl", uriComponents.toUriString());
        node.put("vivoUrl", vivoUrl != null ? vivoUrl : "");
        node.put("uiUrl", uiUrl != null ? uiUrl : "");
        node.put("startYear", startYear != null ? startYear : "");
        node.put("endYear", endYear != null ? endYear : "");

        if (view != null && view.getLazyReferences() != null) {
            fetchAndAttachLazyReferences(node, view.getLazyReferences(), startYear, endYear);
        }
        return node;
    }

    protected void fetchAndAnsAttachLazyReferences(ObjectNode node, List<ExportFieldView> lazyReferences, String startYear, String endYear) {
        if (lazyReferences == null || node == null) return;

        lazyReferences.stream()
            .filter(lr -> lr != null && lr.getField() != null && node.hasNonNull(lr.getField()))
            .forEach(lazyReference -> {
                try {
                    JsonNode reference = node.path(lazyReference.getField());
                    List<String> ids = extractIds(reference);

                    if (!ids.isEmpty()) {
                        List<Individual> ref = fetchLazyReference(lazyReference, ids, startYear, endYear);

                        if (ref != null && !ref.isEmpty()) {
                            node.set(lazyReference.getField(), mapper.valueToTree(ref));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to fetch lazy reference for field " + lazyReference.getField() + ": " + e.getMessage());
                }
            }
        );
    }

    protected List<String> extractIds(JsonNode reference) {
        List<String> ids = new ArrayList<>();
        if (reference == null || reference.isNull()) {
            return ids;
        }

        if (reference.isArray() && !reference.isEmpty()) {
            ids = StreamSupport.stream(reference.spliterator(), false)
                    .filter(rn -> rn != null && rn.has(ID))
                    .map(rn -> rn.get(ID))
                    .filter(idNode -> idNode != null && !idNode.isNull())
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
        } else if (reference.has(ID)) {
            JsonNode singleIdNode = reference.get(ID);
            if (singleIdNode != null && !singleIdNode.isNull()) {
                ids.add(singleIdNode.asText());
            }
        }

        return ids;
    }

    protected void fetchAndAttachLazyReferences(ObjectNode node, List<ExportFieldView> lazyReferences, String startYear, String endYear) {
        if (lazyReferences == null || node == null) return;
        lazyReferences.stream()
            .filter(lazyReference -> lazyReference != null && lazyReference.getField() != null && node.hasNonNull(lazyReference.getField()))
            .forEach(lazyReference -> {
                try {
                    JsonNode reference = node.path(lazyReference.getField());
                    List<String> ids = extractIds(reference);
                    System.out.println("\n\n ADE:fetchAndAttachLazyReferences ids"+ ids);

                    if (!ids.isEmpty()) {
                        List<Individual> ref = fetchLazyReference(lazyReference, ids, startYear, endYear);

                        if (ref != null && !ref.isEmpty()) {
                            node.set(lazyReference.getField(), mapper.valueToTree(ref));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to fetch lazy reference for field " + lazyReference.getField() + ": " + e.getMessage());
                }
            }
        );
    }

    protected List<Individual> fetchLazyReference(ExportFieldView lazyReference, List<String> ids, String startYear, String endYear) {
        if (lazyReference == null || ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        boolean hasYears = startYear != null && !startYear.trim().isEmpty()
                        && endYear != null && !endYear.trim().isEmpty();

        List<FilterArg> filters = new ArrayList<>();
        if (lazyReference.getFilters() != null) {
            for (var filter : lazyReference.getFilters()) {
                if (filter == null || filter.getValue() == null) continue;

                String value = filter.getValue();
                if (value.contains("${startYear}") || value.contains("${endYear}")) {
                    if (hasYears) {
                        String processedValue = value
                            .replace("${startYear}", startYear)
                            .replace("${endYear}", endYear);
                        System.out.println("\n\n\nfetchLazyReference:processedValue: " + processedValue + "\n\n");
                        filters.add(FilterArg.of(
                            filter.getField(),
                            Optional.of(processedValue),
                            Optional.of(filter.getOpKey().name()),
                            Optional.empty(), "", ""
                        ));
                    }
                } else {
                    filters.add(FilterArg.of(
                        filter.getField(),
                        Optional.of(filter.getValue()),
                        Optional.of(filter.getOpKey().name()),
                        Optional.empty(), "", ""
                    ));
                }
            }
        }

        // Sort sort = Sort.by(lazyReference.getSort().stream().map(s -> Order.by(s.getField()).with(s.getDirection())).toList());
        Sort sort = Sort.unsorted();
        if (lazyReference.getSort() != null && !lazyReference.getSort().isEmpty()) {
            sort = Sort.by(lazyReference.getSort().stream()
                .filter(s -> s != null && s.getField() != null)
                .map(s -> Order.by(s.getField()).with(s.getDirection()))
                .toList());
        }

        int limit = lazyReference.getLimit();

        return individualRepo.findByIdIn(ids, filters, sort, limit);
    }

    protected void addMargin(final MainDocumentPart mainDocumentPart) {
        final Body body = mainDocumentPart.getJaxbElement().getBody();
        final SectPr sectPr = body.getSectPr();
        final PgMar pgMar = sectPr.getPgMar();

        pgMar.setLeft(BigInteger.valueOf(750));
        pgMar.setRight(BigInteger.valueOf(750));
        pgMar.setTop(BigInteger.valueOf(500));
        pgMar.setBottom(BigInteger.valueOf(500));
    }

    protected void addContent(final MainDocumentPart mainDocumentPart, String html) throws Docx4JException {
        mainDocumentPart.addAltChunk(AltChunkType.Xhtml, html.getBytes(Charset.defaultCharset()));
    }

    protected void createAndAddHeader(
        final WordprocessingMLPackage pkg,
        final String html
    ) throws InvalidFormatException {
        final HeaderPart headerPart = new HeaderPart(new PartName("/word/content-header.xml"));
        pkg.getParts().put(headerPart);
        final Relationship headerRel = pkg.getMainDocumentPart().addTargetPart(headerPart);
        createAndAddHtmlHeader(headerPart, html);
        final HeaderReference headerRef = WML_OBJECT_FACTORY.createHeaderReference();
        headerRef.setId(headerRel.getId());
        headerRef.setType(HdrFtrRef.DEFAULT);
        final List<SectionWrapper> sections = pkg.getDocumentModel().getSections();
        final SectPr lastSectPr = getLastSectionPart(pkg, sections);
        lastSectPr.getEGHdrFtrReferences().add(headerRef);
    }

    protected void createAndAddHtmlHeader(
        final HeaderPart headerPart,
        final String html
    ) throws InvalidFormatException {
        final Hdr hdr = WML_OBJECT_FACTORY.createHdr();
        headerPart.setJaxbElement(hdr);
        final AlternativeFormatInputPart targetpart = createHeaderHtml(
            new PartName("/word/htmlheader.html"),
            html
        );
        final Relationship rel = headerPart.addTargetPart(targetpart);
        final CTAltChunk ac = WML_OBJECT_FACTORY.createCTAltChunk();
        ac.setId(rel.getId());
        hdr.getContent().add(ac);
    }

    protected AlternativeFormatInputPart createHeaderHtml(
        final PartName partName,
        final String html
    ) throws InvalidFormatException {
        final AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(partName);
        afiPart.setBinaryData(html.getBytes(Charset.defaultCharset()));
        afiPart.setContentType(HTML_CONTENT_TYPE);
        return afiPart;
    }

    protected SectPr getLastSectionPart(final WordprocessingMLPackage pkg, final List<SectionWrapper> sections) {
        final SectionWrapper lastSect = sections.get(sections.size() - 1);
        final SectPr currentSectPr = lastSect.getSectPr();
        if (currentSectPr != null) {
            return currentSectPr;
        }
        final SectPr sectPr = WML_OBJECT_FACTORY.createSectPr();
        pkg.getMainDocumentPart().addObject(sectPr);
        lastSect.setSectPr(sectPr);
        return sectPr;
    }

}
