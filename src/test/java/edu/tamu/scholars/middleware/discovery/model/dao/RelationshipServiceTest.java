package edu.tamu.scholars.middleware.discovery.model.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import edu.tamu.scholars.middleware.discovery.dao.RelationshipService;
import edu.tamu.scholars.middleware.discovery.model.Relationship;
import edu.tamu.scholars.middleware.discovery.model.repo.RelationshipRepo;

public class RelationshipServiceTest extends AbstractNestedDocumentServiceTest<edu.tamu.scholars.middleware.discovery.model.generated.Relationship, Relationship, RelationshipRepo, RelationshipService> {

    @Value("classpath:mock/discovery/relationship")
    private Resource mocksDirectory;

    @Override
    protected Resource getMocksDirectory() {
        return mocksDirectory;
    }

    @Override
    protected Class<?> getType() {
        return Relationship.class;
    }

    @Override
    protected Class<?> getNestedDocumentType() {
        return edu.tamu.scholars.middleware.discovery.model.generated.Relationship.class;
    }

}