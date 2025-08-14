package edu.tamu.scholars.middleware.export.service;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import jakarta.servlet.ServletContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
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
        ExportView exportView
    ) throws IOException, JAXBException, Docx4JException {
        final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        final MainDocumentPart mdp = pkg.getMainDocumentPart();

        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        pkg.getMainDocumentPart().addTargetPart(ndp);
        ndp.unmarshalDefaultNumbering();

        ObjectNode json = processDocument(node, exportView);

        String contentHtml = handlebarsService.template(exportView.getContentTemplate(), json);

        String headerHtml = handlebarsService.template(exportView.getHeaderTemplate(), json);

        addMargin(mdp);

        createAndAddHeader(pkg, headerHtml);

        addContent(mdp, contentHtml);

        return pkg;
    }

    protected <D extends AbstractIndexDocument> ObjectNode processDocument(final ObjectNode node, ExportView view) {
        final UriComponents uriComponents = fromCurrentRequest()
            .replacePath(context.getContextPath())
            .replaceQuery(null)
            .build();
        final String serviceUrl = uriComponents.toUriString();
        node.put("serviceUrl", serviceUrl);
        node.put("vivoUrl", vivoUrl);
        node.put("uiUrl", uiUrl);
        fetchAndAttachLazyReferences(node, view.getLazyReferences());
        return node;
    }

    protected void fetchAndAttachLazyReferences(ObjectNode node, List<ExportFieldView> lazyReferences) {
        lazyReferences
            .stream()
            .filter(lazyReference -> node.hasNonNull(lazyReference.getField()))
            .forEach(lazyReference -> {
                JsonNode reference = node.get(lazyReference.getField());
                List<String> ids = extractIds(reference);
                ArrayNode references = node.putArray(lazyReference.getField());

                List<Individual> ref = fetchLazyReference(lazyReference, ids);

                references.addAll((ArrayNode) mapper.valueToTree(ref));
            });
    }

    protected List<String> extractIds(JsonNode reference) {
        List<String> ids = new ArrayList<String>();
        if (reference.isArray()) {
            ids = StreamSupport.stream(reference.spliterator(), true)
                .map(rn -> rn.get(ID).asText())
                .collect(Collectors.toList());
        } else {
            ids.add(reference.get(ID).asText());
        }

        return ids;
    }

    protected List<Individual> fetchLazyReference(ExportFieldView lazyReference, List<String> ids) {
        List<FilterArg> filters = lazyReference.getFilters().stream().map(f -> {
            return FilterArg.of(
                f.getField(),
                Optional.of(f.getValue()),
                Optional.of(f.getOpKey().getKey()),
                Optional.empty()
            );
        }).collect(Collectors.toList());

        Sort sort = Sort.by(
            lazyReference.getSort().stream().map(s -> {
                return Order.by(s.getField()).with(s.getDirection());
            }).collect(Collectors.toList())
        );

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
        try {
            final AlternativeFormatInputPart targetpart = createHeaderHtml(
                new PartName("/word/htmlheader.html"),
                html
            );
            final Relationship rel = headerPart.addTargetPart(targetpart);
            final CTAltChunk ac = WML_OBJECT_FACTORY.createCTAltChunk();
            ac.setId(rel.getId());
            hdr.getContent().add(ac);
        } catch (final UnsupportedEncodingException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
    }

    protected AlternativeFormatInputPart createHeaderHtml(
        final PartName partName,
        final String html
    ) throws InvalidFormatException, UnsupportedEncodingException {
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
