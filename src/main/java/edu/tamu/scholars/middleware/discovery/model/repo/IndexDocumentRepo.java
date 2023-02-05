package edu.tamu.scholars.middleware.discovery.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.repo.custom.IndexDocumentRepoCustom;

@NoRepositoryBean
public interface IndexDocumentRepo<D extends AbstractIndexDocument> extends PagingAndSortingRepository<D, String>, IndexDocumentRepoCustom<D> {

    @Query
    public List<D> findByType(String type);

    @Query
    public List<D> findByIdIn(List<String> ids);

    @Query
    public List<D> findBySyncIds(String syncId);

    @Query
    public List<D> findBySyncIdsIn(List<String> syncIds);

}
