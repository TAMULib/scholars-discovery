package edu.tamu.scholars.middleware.discovery.model.repo;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.tamu.scholars.middleware.discovery.model.Individual;

@RepositoryRestResource(path = "individual", collectionResourceRel = "individual")
public interface IndividualRepo extends IndexDocumentRepo<Individual> {

    @RestResource(exported = true)
    public List<Individual> findBySyncIds(String syncId);

}
