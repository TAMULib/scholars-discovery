package edu.tamu.scholars.middleware.discovery.component.jena;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.NESTED_DELIMITER;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import edu.tamu.scholars.middleware.discovery.annotation.CollectionSource;
import edu.tamu.scholars.middleware.discovery.annotation.FieldSource;
import edu.tamu.scholars.middleware.discovery.annotation.FieldSource.CacheableLookup;
import edu.tamu.scholars.middleware.discovery.annotation.FieldType;
import edu.tamu.scholars.middleware.discovery.annotation.NestedObject;
import edu.tamu.scholars.middleware.discovery.component.Harvester;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.service.CacheService;
import edu.tamu.scholars.middleware.service.TemplateService;
import edu.tamu.scholars.middleware.service.Triplestore;

/**
 * 
 */
public class TriplestoreHarvester implements Harvester {

    private static final Logger logger = LoggerFactory.getLogger(TriplestoreHarvester.class);

    private static final String COLLECTION = "collection";

    private static final String FORWARD_SLASH = "/";

    private static final String HASH_TAG = "#";

    private static final String NESTED = "nested";

    @Autowired
    private Triplestore triplestore;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private CacheService cacheService;

    private final Class<AbstractIndexDocument> type;

    private final List<Field> fields;

    private final List<Field> nestedFields;

    public TriplestoreHarvester(Class<AbstractIndexDocument> type) {
        this.type = type;
        this.fields = FieldUtils.getFieldsListWithAnnotation(type, FieldSource.class);
        this.nestedFields = FieldUtils.getFieldsListWithAnnotation(type, FieldType.class)
            .stream()
            .filter(this::isNestedField)
            .collect(Collectors.toList());
    }

    public Flux<Individual> harvest() {
        CollectionSource source = type.getAnnotation(CollectionSource.class);
        String query = templateService.templateSparql(COLLECTION, source.predicate());
        logger.debug("{}:\n{}", COLLECTION, query);
        QueryExecution queryExecution = triplestore.createQueryExecution(query);
        Iterator<Triple> tripleIterator = queryExecution.execConstructTriples();

        return Flux.fromIterable(() -> tripleIterator)
            .map(this::subject)
            .map(this::harvest)
            .doFinally(onFinally -> queryExecution.close());
    }

    public Individual harvest(String subject) {
        try {
            return createIndividual(subject);
        } catch (Exception e) {
            logger.debug("Error harvesting", e);
            logger.error("Unable to harvest {} {}: {}", type.getSimpleName(), parse(subject), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Class<AbstractIndexDocument> type() {
        return type;
    }

    private Individual createIndividual(String subject)
        throws IllegalArgumentException, SecurityException {
        Individual individual = new Individual();
        individual.setId(parse(subject));
        lookupProperties(individual, subject);
        lookupSyncIds(individual);
        individual.setProxy(type.getSimpleName());

        return individual;
    }

    private void lookupProperties(Individual individual, String subject) {
        fields.parallelStream().forEach(field -> {
            FieldSource source = field.getAnnotation(FieldSource.class);
            try {
                Model model = queryForModel(source, subject);
                List<Object> values = lookupProperty(source, model);

                // get cacheable lookup
                if (source.lookup().length > 0) {
                    Set<Object> uniqueValues = new HashSet<>();
                    boolean isNestedObject = field.isAnnotationPresent(NestedObject.class);
                    for (CacheableLookup lookup : source.lookup()) {
                        FieldSource cacheableSource = getCacheableSource(source, lookup);
                        for (Object value : values) {
                            if (isNestedObject) {
                                String[] parts = ((String) value).split(NESTED_DELIMITER);
                                String[] ids = Arrays.copyOfRange(parts, 1, parts.length);
                                String[] refIds = ids.length > 1 ? Arrays.copyOfRange(ids, 0, ids.length - 1) : new String[] {};
                                String newSubjectId = ids[ids.length - 1];
                                String newSubject = subject.replaceAll("[^/]+$", newSubjectId);
                                for (Object cached : queryForValues(cacheableSource, newSubject)) {
                                    String[] cachedParts = ((String) cached).split(NESTED_DELIMITER, 2);
                                    String label = cachedParts[0];
                                    StringBuilder result = new StringBuilder(label);
                                    if (refIds.length > 0) {
                                        result.append(NESTED_DELIMITER);
                                        result.append(String.join(NESTED_DELIMITER, refIds));
                                    }
                                    if (cachedParts.length > 1) {
                                        result.append(NESTED_DELIMITER);
                                        result.append(cachedParts[1]);
                                    }

                                    uniqueValues.add(result.toString());
                                }
                            } else {
                                String newSubject = ((String) value);
                                for (Object cached : queryForValues(cacheableSource, newSubject)) {
                                    uniqueValues.add(cached);
                                }
                            }
                        }
                    }
                    values = new ArrayList<>(uniqueValues);
                }

                if (!values.isEmpty()) {
                    populate(individual, field, values);
                } else {
                    logger.debug("Could not find values for {}", field.getName());
                }
            } catch (Exception e) {
                logger.error(
                    "Unable to populate individual {} {}: {}\n{}",
                    type.getSimpleName(),
                    parse(subject),
                    e.getMessage(),
                    source.template()
                );
                logger.debug("Error populating individual", e);
            }
        });
    }

    private List<Object> queryForValues(FieldSource source, String subject) {
        String key = toKey(source, subject);
        List<Object> values = cacheService.get(key);
        if (Objects.isNull(values)) {
            Model model = queryForModel(source, subject);
            values = lookupProperty(source, model);
            cacheService.put(key, values);
        }

        return values;
    }

    private String toKey(FieldSource source, String subject) {
        return String.format("%s::%s::%s", source.predicate(), source.template(), subject);
    }

    private Model queryForModel(FieldSource source, String subject) {
        String query = templateService.templateSparql(source.template(), subject);
        logger.debug("{}:\n{}", source.template(), query);
        try (QueryExecution qe = triplestore.createQueryExecution(query)) {
            Model model = qe.execConstruct();
            if (logger.isDebugEnabled()) {
                model.write(System.out, "RDF/XML");
            }

            return model;
        }
    }

    private List<Object> lookupProperty(FieldSource source, Model model) {
        List<Object> values = new ArrayList<>();
        ResIterator resources = model.listSubjects();

        Property property = model.createProperty(source.predicate());

        while (resources.hasNext()) {
            Resource resource = resources.next();
            values.addAll(queryForProperty(source, resource, property));
        }

        return values;
    }

    private List<Object> queryForProperty(FieldSource source, Resource resource, Property property) {
        List<Object> values = new ArrayList<>();
        Set<String> uniqueCheck = source.unique() ? new HashSet<>() : null;
        StmtIterator statements = resource.listProperties(property);
        boolean shouldParse = source.parse();
        boolean shouldSplit = source.split();

        while (statements.hasNext()) {
            Statement statement = statements.next();
            String object = statement.getObject().toString();
            String value = shouldParse ? parse(object) : object;

            value = value.replace("\\\"", "\"");

            int indexOfTypeToken = value.indexOf("^^");
            if (indexOfTypeToken >= 0) {
                int endQuoteIndex = indexOfTypeToken - 1;
                if (value.charAt(0) == '"' && value.charAt(endQuoteIndex) == '"') {
                    value = value.substring(1, endQuoteIndex);
                } else {
                    value = value.substring(0, indexOfTypeToken);
                }
            }

            if (uniqueCheck != null && !uniqueCheck.add(value.toLowerCase())) {
                logger.debug("duplicate value {}", value);
                continue;
            }

            if (shouldSplit) {
                String[] parts = value.split("\\|\\|");
                for (String part : parts) {
                    values.add(part);
                }
            } else {
                values.add(value);
            }
        }

        return values;
    }

    private void populate(
        Individual document,
        Field field,
        List<Object> values
    ) throws IllegalArgumentException {
        if (values.isEmpty()) {
            logger.debug("Could not find values for {}", field.getName());
        } else {
            if (Collection.class.isAssignableFrom(field.getType())) {
                document.getContent().put(field.getName(), values);
            } else {
                document.getContent().put(field.getName(), values.get(0));
            }
        }
    }

    private void lookupSyncIds(Individual individual) {
        Set<String> syncIds = new HashSet<>();
        syncIds.add(individual.getId());
        nestedFields.stream()
            .forEach(field -> {
                Object value = individual.getContent().get(field.getName());
                if (value != null) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        @SuppressWarnings("unchecked")
                        List<String> values = (List<String>) value;
                        values.forEach(v -> addSyncId(syncIds, v));
                    } else {
                        addSyncId(syncIds, (String) value);
                    }
                }
            });
        individual.setSyncIds(new ArrayList<>(syncIds));
    }

    private void addSyncId(Set<String> syncIds, String value) {
        syncIds.addAll(Arrays.asList(value.split(NESTED_DELIMITER)));
    }

    private boolean isNestedField(Field field) {
        return field.getAnnotation(FieldType.class)
            .type()
            .startsWith(NESTED);
    }

    private String subject(Triple triple) {
        return triple.getSubject()
            .toString();
    }

    private String parse(String uri) {
        return uri.substring(uri.lastIndexOf(uri.contains(HASH_TAG) ? HASH_TAG : FORWARD_SLASH) + 1);
    }

    private FieldSource getCacheableSource(FieldSource source, CacheableLookup lookup) {
        return new FieldSource() {
            @Override
            public String template() {
                return lookup.template();
            }

            @Override
            public String predicate() {
                return lookup.predicate();
            }

            @Override
            public boolean parse() {
                return source.parse();
            }

            @Override
            public boolean unique() {
                return source.unique();
            }

            @Override
            public boolean split() {
                return source.split();
            }

            @Override
            public CacheableLookup[] lookup() {
                return new CacheableLookup[] {};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                throw new UnsupportedOperationException("Unimplemented method 'annotationType'");
            }
        };
    }

}
