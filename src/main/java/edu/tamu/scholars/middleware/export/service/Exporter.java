package edu.tamu.scholars.middleware.export.service;

import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.export.argument.ExportArg;
import edu.tamu.scholars.middleware.export.exception.UnsupportedExporterTypeException;

/**
 * 
 */
public interface Exporter {

    public String type();

    public String contentDisposition(String filename);

    public String contentType();

    public default StreamingResponseBody streamIndividuals(Flux<Individual> cursor, List<ExportArg> export) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support export field exports",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividuals(List<Individual> individuals, String name) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support exporting multiple individuals",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividualsByIds(List<String> ids, String name, String startYear, String endYear) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support exporting multiple individual ids with year range.",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividual(Individual individual, String name) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support individual templated exports",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividual(Individual individual, String name, String startYear, String endYear) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support individual templated exports",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividualById(String id, String name, String startYear, String endYear) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support exporting multiple individual ids with year range.",
            type()
        ));
    }

    public default StreamingResponseBody streamIndividuals(Individual individual, List<ExportArg> export) {
        throw new UnsupportedExporterTypeException(String.format(
            "%s exporter does not support export field exports",
            type()
        ));
    }

}
