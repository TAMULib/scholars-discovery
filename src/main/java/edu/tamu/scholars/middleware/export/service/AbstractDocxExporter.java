package edu.tamu.scholars.middleware.export.service;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.valueOf;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static jakarta.persistence.GenerationType.values;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.io.IOException;
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

//     protected WordprocessingMLPackage createDocx(
//         ObjectNode node,
//         ExportView exportView,
//         String startYear,
//         String endYear
// ) throws JAXBException, Docx4JException, IOException {
//     final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
//     final MainDocumentPart mdp = pkg.getMainDocumentPart();

//     final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
//     pkg.getMainDocumentPart().addTargetPart(ndp);
//     ndp.unmarshalDefaultNumbering();

//     ObjectNode json = processDocument(node, exportView, startYear, endYear);
//     System.out.println("\n\n\n WordprocessingMLPackage createDocx json: " + mapper.writeValueAsString(json));

//     json.put("startYear", startYear != null ? startYear : "");
//     json.put("endYear", endYear != null ? endYear : "");

//     String contentHtml = handlebarsService.template(exportView.getContentTemplate(), json);
//     String headerHtml = handlebarsService.template(exportView.getHeaderTemplate(), json);

//     addMargin(mdp);
//     createAndAddHeader(pkg, headerHtml);
//     addContent(mdp, contentHtml);
//     System.out.println("\n\n\n WordprocessingMLPackage createDocx contentHtml: " + mapper.writeValueAsString(contentHtml));
//     return pkg;
//     }
    
    protected WordprocessingMLPackage createDocx(
        ObjectNode node,
        ExportView exportView,
        String startYear,
        String endYear
    ) throws JAXBException, Docx4JException, IOException {
        System.out.println("\n\n\n IN AbstractDocxExporter CREATEDOCX: ");
        final WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        final MainDocumentPart mdp = pkg.getMainDocumentPart();

        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        pkg.getMainDocumentPart().addTargetPart(ndp);
        ndp.unmarshalDefaultNumbering();

        ObjectNode json = processDocument(node, exportView, startYear, endYear);

        if (startYear != null) {
            json.put("startYear", startYear);
        }
        if (endYear != null) {
            json.put("endYear", endYear);
        }

        System.out.println("\n\n\n AbstractDocxExporter cerateDocx json: " + mapper.writeValueAsString(json));
        if (!json.has("publications")) {
            ArrayNode publicationsArray = mapper.createArrayNode();
            publicationsArray.add(json.deepCopy());
            
            ObjectNode root = mapper.createObjectNode();
            root.set("publications", publicationsArray);
            root.put("startYear", startYear != null ? startYear : "");
            root.put("endYear", endYear != null ? endYear : "");
            
            json = root;
        }
        System.out.println("\n\n\n IN AbstractDocxExporter CREATEDOCX: proecssed Document  updated json: "+ mapper.writeValueAsString(json) );
        String contentHtml = handlebarsService.template(exportView.getContentTemplate(), json);

        String headerHtml = handlebarsService.template(exportView.getHeaderTemplate(), json);

        addMargin(mdp);

        createAndAddHeader(pkg, headerHtml);

        addContent(mdp, contentHtml);
        System.out.println("\n\n\n END AbstractDocxExporter CREATEDOCX: ");
        return pkg;
    }

    protected ObjectNode processDocument(final ObjectNode node, ExportView view, String startYear, String endYear) {
        System.out.println("\n\n Abstract DOC Exporter: processDocument: \n view name: "+view.getName()+ "\n Start Year"+ startYear+ "\n End Year"+ endYear );
        final UriComponents uriComponents = fromCurrentRequest()
            .replacePath(context.getContextPath())
            .replaceQuery(null)
            .build();
        System.out.println("\n\n Abstract DOC Exporter: processDocument:uriComponents: "+ uriComponents);
        final String serviceUrl = uriComponents.toUriString();
        System.out.println("\n\n Abstract DOC Exporter: processDocument:serviceUrl: "+ serviceUrl);
        node.put("serviceUrl", serviceUrl);
        node.put("vivoUrl", vivoUrl);
        node.put("uiUrl", uiUrl);
        fetchAndAttachLazyReferences(node, view.getLazyReferences(), startYear, endYear);
        return node;
    }

    protected void fetchAndAttachLazyReferences(ObjectNode node, List<ExportFieldView> lazyReferences, String startYear, String endYear) {
        lazyReferences
            .stream()
            .filter(lazyReference -> node.hasNonNull(lazyReference.getField()))
            .forEach(lazyReference -> {
                JsonNode reference = node.get(lazyReference.getField());
                List<String> ids = extractIds(reference);
                System.out.printf("\n\n\nAbs DOCS fetchAndAttachLazyReferences: ", reference.values());
                ArrayNode references = node.putArray(lazyReference.getField());

                List<Individual> ref = fetchLazyReference(lazyReference, ids, startYear, endYear);

                references.addAll((ArrayNode) mapper.valueToTree(ref));
            });
    }

    protected List<String> extractIds(JsonNode reference) {

        List<String> ids = new ArrayList<>();
        if (reference == null || reference.isNull() || reference.isMissingNode()) {
            return ids;
        }

        if (reference.isArray()) {
            ids = StreamSupport.stream(reference.spliterator(), true)
            .filter(rn -> rn != null && rn.hasNonNull(ID))
            .map(rn -> rn.get(ID).asText())
            .collect(Collectors.toList());
        } else if (reference.hasNonNull(ID)) {
            ids.add(reference.get(ID).asText());
        }

        return ids;
    }

    protected List<Individual> fetchLazyReference(ExportFieldView lazyReference, List<String> ids, String startYear, String endYear) {
    System.out.println("\n\n Abstract DOCXexporter: fetchLazyReference: startYear=" + startYear + ", endYear=" + endYear);

    final boolean hasStart = startYear != null && !startYear.isBlank() && !startYear.equals("*");
    final boolean hasEnd = endYear != null && !endYear.isBlank() && !endYear.equals("*");

    final String solrStartDate = hasStart ? startYear.trim() + "-01-01T00:00:00Z" : "*";
    final String solrEndDate = hasEnd ? endYear.trim() + "-12-31T23:59:59Z" : "*";

    List<FilterArg> filters = lazyReference.getFilters().stream().map(f -> {
        String filterValue = f.getValue();
        System.out.println("\n\n\n fetchLazyReference 1filterValue: " + filterValue);
        if (filterValue != null) {
            // Replace full expressions if template string exists in database metadata
            filterValue = filterValue
                .replace("${startYear}-01-01T00:00:00Z", solrStartDate)
                .replace("${endYear}-12-31T23:59:59Z", solrEndDate)
                .replace("{startYear}-01-01T00:00:00Z", solrStartDate)
                .replace("{endYear}-12-31T23:59:59Z", solrEndDate)
                .replace("${startYear}", hasStart ? startYear.trim() : "*")
                .replace("${endYear}", hasEnd ? endYear.trim() : "*");

            filterValue = filterValue.replace("*-01-01T00:00:00Z", "*");
            filterValue = filterValue.replace("*-12-31T23:59:59Z", "*");
            System.out.println("\n\n\n fetchLazyReference 2filterValue: " + filterValue);
        }

        return FilterArg.of(
            f.getField(),
            Optional.ofNullable(filterValue),
            Optional.ofNullable(f.getOpKey() != null ? f.getOpKey().getKey() : null),
            Optional.empty()
        );
    }).toList();

    Sort sort = Sort.by(
        lazyReference.getSort().stream()
            .map(s -> Order.by(s.getField()).with(s.getDirection()))
            .toList()
    );

    int limit = lazyReference.getLimit();

    return individualRepo.findByIdIn(ids, filters, sort, limit);
}

    protected List<Individual> fetchLazyReference2(ExportFieldView lazyReference, List<String> ids, String startYear, String endYear) {
        System.out.println("\n\n\n Abstract DOCXexporter: fetchLazyReference: ");
        /*
        List<FilterArg> filters = lazyReference.getFilters().stream().map(f -> 
            FilterArg.of(
            f.getField(),
            Optional.of(f.getValue()),
            Optional.of(f.getOpKey().getKey()),
            Optional.empty()
        )).toList();

        Sort sort = Sort.by(lazyReference.getSort().stream().map(s -> Order.by(s.getField()).with(s.getDirection())).toList());

        int limit = lazyReference.getLimit();

        return individualRepo.findByIdIn(ids, filters, sort, limit);
        */
       final String safeStart = (startYear != null && !startYear.isBlank()) ? startYear.trim() : "*";
       System.out.println("\n\n ABS safeStart: "+ safeStart);
       final String safeEnd = (endYear != null && !endYear.isBlank()) ? endYear.trim() : "*";
        System.out.println("\n\n ABS safeEnd: "+ safeEnd);
       List<FilterArg> filters = lazyReference.getFilters().stream().map(f -> {
        String filterValue = f.getValue();
        System.out.println("\n\n ABS filterValue: "+ filterValue);
        if (filterValue != null) {
            filterValue = filterValue
                .replace("${startYear}", safeStart)
                .replace("${endYear}", safeEnd)
                .replace("{startYear}", safeStart)
                .replace("{endYear}", safeEnd);

            if (filterValue.startsWith("-01-01")) {
                filterValue = (safeStart.equals("*") ? "1900" : safeStart) + filterValue;
            }
      }

      return FilterArg.of(
            f.getField(),
            Optional.ofNullable(filterValue),
            Optional.ofNullable(f.getOpKey() != null ? f.getOpKey().getKey() : null),
            Optional.empty()
        );
      }).toList();

      boolean hasDateFilter = filters.stream().anyMatch(filter -> "publicationDate".equals(filter));
    
      if (!hasDateFilter && (!safeStart.equals("*") || !safeEnd.equals("*"))) {
        String startDateStr = safeStart.equals("*") ? "*" : safeStart + "-01-01T00:00:00Z";
        String endDateStr = safeEnd.equals("*") ? "*" : safeEnd + "-12-31T23:59:59Z";
        String dateRangeQuery = String.format("[%s TO %s]", startDateStr, endDateStr);

        List<FilterArg> updatedFilters = new ArrayList<>(filters);
        updatedFilters.add(FilterArg.of(
            "publicationDate",
            Optional.of(dateRangeQuery),
            Optional.of("BETWEEN"),
            Optional.empty()
        ));
        filters = updatedFilters;
      }

      Sort sort = Sort.by(
        lazyReference.getSort().stream()
            .map(s -> Order.by(s.getField()).with(s.getDirection()))
            .toList()
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
