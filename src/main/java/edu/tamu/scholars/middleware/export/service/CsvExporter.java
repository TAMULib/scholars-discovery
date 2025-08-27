package edu.tamu.scholars.middleware.export.service;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.NESTED_DELIMITER;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import edu.tamu.scholars.middleware.config.model.ExportConfig;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.argument.ExportArg;
import edu.tamu.scholars.middleware.utility.DateFormatUtility;

/**
 * 
 */
@Service
public class CsvExporter implements Exporter {

    private static final String TYPE = "csv";

    private static final String CONTENT_TYPE = "text/csv";

    private static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=%s.csv";

    private static final String DELIMITER = "||";

    @Autowired
    private ExportConfig config;

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
    public StreamingResponseBody streamIndividuals(Flux<Individual> individuals, List<ExportArg> export) {
        return outputStream -> {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] headers = getColumnHeaders(export);
            CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader(headers)
                .build();
            List<String> properties = export.stream()
                .map(e -> e.getField())
                .toList();
            try (CSVPrinter printer = new CSVPrinter(outputStreamWriter, format)) {
                individuals.doOnComplete(() -> {
                    try {
                        printer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to flush CSV printer", e);
                    }
                }).subscribe(
                    individual -> {
                        try {
                            List<Object> row = getRow(individual, properties);
                            printer.printRecord(row.toArray(new Object[row.size()]));
                        } catch (IllegalArgumentException | IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Failed mapping and printing individuals", e);
                        }
                    }, e -> {
                        e.printStackTrace();
                        throw new RuntimeException("Failed attempting to stream individuals", e);
                    }
                );
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                outputStreamWriter.close();
            }
        };
    }

    private String[] getColumnHeaders(List<ExportArg> export) {
        List<String> columnHeaders = new ArrayList<>();
        for (ExportArg exp : export) {
            columnHeaders.add(exp.getLabel());
        }
        return columnHeaders.toArray(new String[columnHeaders.size()]);
    }

    private List<Object> getRow(
        Individual individual,
        List<String> properties
    ) throws IllegalArgumentException {
        Map<String, Object> content = individual.getContent();
        List<Object> row = new ArrayList<>();
        for (String property : properties) {
            if (property.equals(config.getIndividualKey())) {
                row.add(String.format("%s/%s", config.getIndividualBaseUri(), individual.getId()));
                continue;
            }

            String data = StringUtils.EMPTY;

            if (content.containsKey(property)) {

                Object value = content.get(property);

                if (List.class.isAssignableFrom(value.getClass())) {

                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) value;

                    if (!values.isEmpty()) {
                        data = String.join(DELIMITER, values.stream()
                            .map(this::serialize)
                            .toList());
                    }

                } else if (Date.class.isAssignableFrom(value.getClass())) {
                    data = DateFormatUtility.format((Date) value);
                } else {
                    data = (String) value;
                }
            }
            row.add(serialize(data));
        }
        return row;
    }

    private String serialize(String value) {
        return value.contains(NESTED_DELIMITER)
            ? value.substring(0, value.indexOf(NESTED_DELIMITER))
            : value;
    }

}
