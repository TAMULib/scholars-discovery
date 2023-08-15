package edu.tamu.scholars.middleware.discovery.component.jena;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.NESTED_DELIMITER;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.InvalidPropertyURIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.scholars.middleware.discovery.annotation.CollectionSource;
import edu.tamu.scholars.middleware.discovery.annotation.FieldSource;
import edu.tamu.scholars.middleware.discovery.annotation.FieldType;
import edu.tamu.scholars.middleware.discovery.component.Harvester;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.service.TemplateService;
import edu.tamu.scholars.middleware.service.Triplestore;
import reactor.core.publisher.Flux;

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

    private final Class<AbstractIndexDocument> type;

    private final List<TypeOp> propertySourceTypeOps;

    private final List<Field> nestedFields;

    public TriplestoreHarvester(Class<AbstractIndexDocument> type) {
        this.type = type;
        this.propertySourceTypeOps = FieldUtils.getFieldsListWithAnnotation(type, FieldSource.class)
            .stream()
            .map(this::getTypeOp)
            .collect(Collectors.toList());
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

    private Individual createIndividual(String subject) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Individual individual = new Individual();
        individual.setId(parse(subject));
        lookupProperties(individual, subject);
        lookupSyncIds(individual);
        individual.setProxy(type.getSimpleName());

        return individual;
    }

    private void lookupProperties(Individual individual, String subject) {
        propertySourceTypeOps.parallelStream().forEach(typeOp -> {
            try {
                FieldSource source = typeOp.getPropertySource();
                Model model = queryForModel(source, subject);
                List<Object> values = lookupProperty(typeOp, source, model);
                if (!values.isEmpty()) {
                    populate(individual, typeOp.getField(), values);
                } else {
                    logger.debug("Could not find values for {}", typeOp.getField().getName());
                }
            } catch (Exception e) {
                logger.error("Unable to populate individual {} {}: {}", type.getSimpleName(), parse(subject), e.getMessage());
                logger.debug("Error populating individual", e);
            }
        });
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

    private List<Object> lookupProperty(TypeOp typeOp, FieldSource source, Model model) {
        List<Object> values = new ArrayList<>();
        ResIterator resources = model.listSubjects();
        while (resources.hasNext()) {
            Resource resource = resources.next();
            values.addAll(queryForProperty(typeOp, source, model, resource));
        }
        return values;
    }

    private List<Object> queryForProperty(TypeOp typeOp, FieldSource source, Model model, Resource resource) {
        List<Object> values = new ArrayList<>();
        StmtIterator statements;
        try {
            statements = resource.listProperties(model.createProperty(source.predicate()));
        } catch (InvalidPropertyURIException e) {
            logger.error("{} lookup by {}: {}", typeOp.getField().getName(), source.predicate(), e.getMessage());
            throw e;
        }

        while (statements.hasNext()) {
            Statement statement = statements.next();
            String object = statement.getObject().toString();
            String value = source.parse() ? parse(object) : object;
            value = value.replace("\\\"", "\"");
            if (value.contains("^^")) {
                value = value.substring(0, value.indexOf("^^"));
            }
            if (source.unique() && values.stream().map(v -> v.toString()).anyMatch(value::equalsIgnoreCase)) {
                logger.debug("{} has duplicate value {}", typeOp.getField().getName(), value);
            } else {
                if (source.split()) {
                    for (String v : value.split("\\|\\|")) {
                        values.add(typeOp.type(v));
                    }
                } else {
                    values.add(typeOp.type(value));
                }
            }
        }

        return values;
    }

    private void populate(Individual document, Field field, List<Object> values) throws IllegalArgumentException, IllegalAccessException {
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
        Set<String> syncIds = new HashSet<String>();
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
        String[] vParts = value.split(NESTED_DELIMITER);
        for (int i = 1; i < vParts.length; i++) {
            syncIds.add(vParts[i]);
        }
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

    private TypeOp getTypeOp(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Class<?> collectionType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            if (String.class.isAssignableFrom(collectionType)) {
                return new StringOp(field);
            } else if (Integer.class.isAssignableFrom(collectionType)) {
                return new IntegerOp(field);
            } else if (Float.class.isAssignableFrom(collectionType)) {
                return new FloatOp(field);
            } else if (Double.class.isAssignableFrom(collectionType)) {
                return new DoubleOp(field);
            }
        } else if (String.class.isAssignableFrom(field.getType())) {
            return new StringOp(field);
        } else if (Integer.class.isAssignableFrom(field.getType())) {
            return new IntegerOp(field);
        } else if (Float.class.isAssignableFrom(field.getType())) {
            return new FloatOp(field);
        } else if (Double.class.isAssignableFrom(field.getType())) {
            return new DoubleOp(field);
        }
        return new StringOp(field);
    }

    private interface TypeOp {
        public Object type(String value);

        public Field getField();

        public FieldSource getPropertySource();
    }

    private abstract class AbstractTypeOp implements TypeOp {
        private final Field field;

        private final FieldSource source;

        public AbstractTypeOp(Field field) {
            this.field = field;
            this.source = field.getAnnotation(FieldSource.class);
        }

        public Field getField() {
            return field;
        }

        public FieldSource getPropertySource() {
            return source;
        }
    }

    private class StringOp extends AbstractTypeOp {
        public StringOp(Field field) {
            super(field);
        }

        public Object type(String value) {
            return value;
        }
    }

    private class IntegerOp extends AbstractTypeOp {
        public IntegerOp(Field field) {
            super(field);
        }

        public Object type(String value) {
            return Integer.parseInt(value);
        }
    }

    private class FloatOp extends AbstractTypeOp {
        public FloatOp(Field field) {
            super(field);
        }

        public Object type(String value) {
            return Float.parseFloat(value);
        }
    }

    private class DoubleOp extends AbstractTypeOp {
        public DoubleOp(Field field) {
            super(field);
        }

        public Object type(String value) {
            return Double.parseDouble(value);
        }
    }

}
