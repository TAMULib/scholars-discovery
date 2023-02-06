package edu.tamu.scholars.middleware.discovery.model.repo;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.repo.custom.IndexDocumentRepoCustom;

@NoRepositoryBean
public interface IndexDocumentRepo<D extends AbstractIndexDocument> extends PagingAndSortingRepository<D, String>, IndexDocumentRepoCustom<D> {

}
