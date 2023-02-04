package edu.tamu.scholars.middleware.discovery.model.repo;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface IndexRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    long count();

}
