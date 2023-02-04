package edu.tamu.scholars.middleware.discovery.model.repo;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.repo.custom.IndexDocumentRepoCustom;

@NoRepositoryBean
public interface IndexDocumentRepo<D extends AbstractIndexDocument> extends IndexCrudRepository<D, String>, IndexDocumentRepoCustom<D> {

    @Override
    @RestResource(exported = false)
    public <S extends D> S save(S document);

    @Override
    @RestResource(exported = false)
    public void delete(D document);

    public List<D> findByType(String type);

    public List<D> findByIdIn(List<String> ids);

    public List<D> findBySyncIds(String syncId);

    public List<D> findBySyncIdsIn(List<String> syncIds);

}
