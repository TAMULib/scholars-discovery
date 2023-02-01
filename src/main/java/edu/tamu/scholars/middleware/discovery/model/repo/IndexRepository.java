package edu.tamu.scholars.middleware.discovery.model.repo;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface IndexRepository<T, ID extends Serializable> extends Repository<T, ID> {

	long count();

}
