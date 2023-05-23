package edu.tamu.scholars.middleware.export.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.math.NumberUtils;
import org.docx4j.Docx4J;
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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.export.exception.ExportException;
import edu.tamu.scholars.middleware.service.TemplateService;
import edu.tamu.scholars.middleware.utility.DateFormatUtility;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportFieldView;
import edu.tamu.scholars.middleware.view.model.ExportView;
import edu.tamu.scholars.middleware.view.model.Filter;
import edu.tamu.scholars.middleware.view.model.repo.DisplayViewRepo;

@Service
public class ZipDocxExporter implements Exporter {

    private static final int MAX_DOCUMENT_BATCH_SIZE = 200;

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\[(.*?) TO (.*?)\\]$");

    private static final String TYPE = "zip";

    private static final String CONTENT_TYPE = "application/zip";

    private static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=%s.zip";

    private static final ContentType HTML_CONTENT_TYPE = new ContentType("text/html");

    private static final ObjectFactory WML_OBJECT_FACTORY = Context.getWmlObjectFactory();

    @Autowired
    private DisplayViewRepo displayViewRepo;

    @Autowired
    private IndividualRepo individualRepo;

    @Autowired
    private TemplateService handlebarsService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${vivo.base-url:http://localhost:8080/vivo}")
    private String vivoUrl;

    @Value("${ui.url:http://localhost:4200}")
    private String uiUrl;

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

            if (exportView.get().getMultipleReference() != null) {

                ExportFieldView multipleReference = exportView.get().getMultipleReference();

                JsonNode reference = node.get(multipleReference.getField());

                List<String> ids = new ArrayList<String>();
                if (reference.isArray()) {
                    ids = StreamSupport.stream(reference.spliterator(), false).map(rn -> rn.get("id").asText()).collect(Collectors.toList());
                } else {
                    ids.add(reference.get("id").asText());
                }

                List<AbstractIndexDocument> referenceDocuments = fetchLazyReference(ids);

                File zipFile = File.createTempFile(document.getId(), ".zip");

                try (
                    FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
                    ZipOutputStream zos = new ZipOutputStream(outputStream)
                ) {
                    for (AbstractIndexDocument refDoc : referenceDocuments) {
                        final ObjectNode refNode = mapper.valueToTree(refDoc);

                        File refDocFile = File.createTempFile(refDoc.getId(), ".docx");

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
            } else {
                throw new ExportException("Zip docx exporter requires multipleReference");
            }
        };
    }

    private void zipFile(ZipOutputStream zos, File file) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }

        zos.closeEntry();
    }

    // maybe rename method
    private <D extends AbstractIndexDocument> ObjectNode processDocument(ObjectNode node, ExportView view) {
        node.put("vivoUrl", vivoUrl);
        node.put("uiUrl", uiUrl);

        fetchLazyReferences(node, view.getLazyReferences());

        // apply field value to fetched lazy reference
        view.getFieldViews().forEach(fieldView -> {

            filter(node, fieldView);

            sort(node, fieldView);

            limit(node, fieldView);
        });
        return node;
    }

    private void fetchLazyReferences(ObjectNode node, List<String> lazyReferences) {
        lazyReferences
            .stream()
            .filter(lr -> node.hasNonNull(lr))
            .forEach(lazyReference -> {
                JsonNode reference = node.get(lazyReference);
                List<String> ids = new ArrayList<String>();
                if (reference.isArray()) {
                    ids = StreamSupport.stream(reference.spliterator(), false).map(rn -> rn.get("id").asText()).collect(Collectors.toList());
                } else {
                    ids.add(reference.get("id").asText());
                }
                ArrayNode references = node.putArray(lazyReference);
                references.addAll((ArrayNode) mapper.valueToTree(fetchLazyReference(ids)));
            });
    }

    private List<AbstractIndexDocument> fetchLazyReference(List<String> ids) {
        List<AbstractIndexDocument> documents = new ArrayList<AbstractIndexDocument>();
        while (ids.size() >= MAX_DOCUMENT_BATCH_SIZE) {
            documents.addAll(individualRepo.findByIdIn(ids.subList(0, MAX_DOCUMENT_BATCH_SIZE)));
            ids = ids.subList(MAX_DOCUMENT_BATCH_SIZE, ids.size());
        }
        documents.addAll(individualRepo.findByIdIn(ids));
        return documents;
    }

    // in memory filtering as already fetched all related individuals via lazy fetching
    private void filter(ObjectNode node, ExportFieldView fieldView) {
        String field = fieldView.getField();
        List<JsonNode> filtered = node.has(field)
            ? StreamSupport.stream(node.get(field).spliterator(), false)
                .filter((n) -> {
                    for (Filter filter : fieldView.getFilters()) { 
                        // cleanup: return single streaming filter with inner switch
                        switch (filter.getOpKey()) {
                            case BETWEEN:
                                // could sort first and stop search but then require break from iteration
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> {

                                        Matcher rangeMatcher = RANGE_PATTERN.matcher(filter.getValue());
                                        if (rangeMatcher.matches()) {
                                            String start = rangeMatcher.group(1);
                                            String end = rangeMatcher.group(2);

                                            String v = fn.asText();

                                            try {
                                                ZonedDateTime zdtv = DateFormatUtility.parse(v);

                                                ZonedDateTime zds = DateFormatUtility.parse(start);
                                                ZonedDateTime zde = DateFormatUtility.parse(end);

                                                return (zds.isBefore(zdtv) || zds.isEqual(zdtv)) && (zdtv.isBefore(zde) || zdtv.isEqual(zde));
                                            } catch (ParseException pe) {
                                                if (NumberUtils.isParsable(v)) {
                                                    Double dv = Double.parseDouble(v);

                                                    Double ds = Double.parseDouble(start);
                                                    Double de = Double.parseDouble(end);

                                                    return ds <= dv && dv <= de;
                                                } else {
                                                    return start.compareTo(v) <= 0 && v.compareTo(end) <= 0;
                                                }
                                            }

                                        } else {
                                            throw new RuntimeException("Not valid between filter");
                                        }
                                    });
                            case CONTAINS:
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> fn.asText().contains(filter.getValue()));
                            case STARTS_WITH:
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> fn.asText().startsWith(filter.getValue()));
                            case ENDS_WITH:
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> fn.asText().endsWith(filter.getValue()));
                            case FUZZY:
                                throw new UnsupportedOperationException("FUZZY: In memory fuzzy filter not yet supported");
                            case EXPRESSION:
                                throw new UnsupportedOperationException("EXPRESSION: In memory expression filter not yet supported");
                            case RAW:
                                throw new UnsupportedOperationException("RAW: In memory raw filter not yet supported");
                            case NOT_EQUALS:
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> !fn.asText().equals(filter.getValue()));
                            case EQUALS:
                            default:
                                return StreamSupport.stream(n.get(filter.getField()).spliterator(), false)
                                    .anyMatch((fn) -> fn.asText().equals(filter.getValue()));
                        }
                    }
                    return false;
                }).collect(Collectors.toList())
            : new ArrayList<>();
        ArrayNode references = node.putArray(field);
        references.addAll(filtered);
    }

    private void sort(ObjectNode node, ExportFieldView fieldView) {
        String field = fieldView.getField();
        fieldView.getSort().forEach(sort -> {
            List<JsonNode> sorted = node.has(field)
                ? StreamSupport.stream(node.get(field).spliterator(), false)
                    .sorted((sn1, sn2) -> {
                        JsonNode jn1 = sn1.get(sort.getField());
                        JsonNode jn2 = sn2.get(sort.getField());
                        String n1 = jn1 != null ? jn1.asText() : "";
                        String n2 = jn2 != null ? jn2.asText() : "";
                        try {
                            ZonedDateTime ld1 = DateFormatUtility.parse(n1);
                            ZonedDateTime ld2 = DateFormatUtility.parse(n2);
                            return sort.getDirection().equals(Direction.ASC) ? ld1.compareTo(ld2) : ld2.compareTo(ld1);
                        } catch (ParseException pe) {
                            if (NumberUtils.isParsable(n1) && NumberUtils.isParsable(n2)) {
                                Double d1 = Double.parseDouble(n1);
                                Double d2 = Double.parseDouble(n2);
                                return sort.getDirection().equals(Direction.ASC) ? d1.compareTo(d2) : d2.compareTo(d1);
                            } else {
                                return sort.getDirection().equals(Direction.ASC) ? n1.compareTo(n2) : n2.compareTo(n1);
                            }
                        }

                    }).collect(Collectors.toList())
                : new ArrayList<>();
            ArrayNode references = node.putArray(field);
            references.addAll(sorted);
        });
    }

    private void limit(ObjectNode node, ExportFieldView fieldView) {
        String field = fieldView.getField();
        List<JsonNode> limited = node.has(field)
            ? StreamSupport.stream(node.get(field).spliterator(), false)
                .limit(fieldView.getLimit())
                    .collect(Collectors.toList())
            : new ArrayList<>();
        ArrayNode references = node.putArray(field);
        references.addAll(limited);
    }

    

    private void addMargin(final MainDocumentPart mainDocumentPart) {
        final Body body = mainDocumentPart.getJaxbElement().getBody();
        final SectPr sectPr = body.getSectPr();
        final PgMar pgMar = sectPr.getPgMar();

        pgMar.setLeft(BigInteger.valueOf(750));
        pgMar.setRight(BigInteger.valueOf(750));
        pgMar.setTop(BigInteger.valueOf(500));
        pgMar.setBottom(BigInteger.valueOf(500));
    }

    private void addContent(final MainDocumentPart mainDocumentPart, String html) throws Docx4JException {
        mainDocumentPart.addAltChunk(AltChunkType.Xhtml, html.getBytes(Charset.defaultCharset()));
    }

    private void createAndAddHeader(final WordprocessingMLPackage pkg, final String html) throws InvalidFormatException {
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

    private void createAndAddHtmlHeader(final HeaderPart headerPart, final String html) throws InvalidFormatException {
        final Hdr hdr = WML_OBJECT_FACTORY.createHdr();
        headerPart.setJaxbElement(hdr);
        try {
            final AlternativeFormatInputPart targetpart = createHeaderHtml(new PartName("/word/htmlheader.html"), html);
            final Relationship rel = headerPart.addTargetPart(targetpart);
            final CTAltChunk ac = WML_OBJECT_FACTORY.createCTAltChunk();
            ac.setId(rel.getId());
            hdr.getContent().add(ac);
        } catch (final UnsupportedEncodingException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
    }

    private AlternativeFormatInputPart createHeaderHtml(final PartName partName, final String html) throws InvalidFormatException, UnsupportedEncodingException {
        final AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(partName);
        afiPart.setBinaryData(html.getBytes(Charset.defaultCharset()));
        afiPart.setContentType(HTML_CONTENT_TYPE);
        return afiPart;
    }

    private SectPr getLastSectionPart(final WordprocessingMLPackage pkg, final List<SectionWrapper> sections) {
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
