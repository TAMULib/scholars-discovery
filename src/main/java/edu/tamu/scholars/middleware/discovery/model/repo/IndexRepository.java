package edu.tamu.scholars.middleware.discovery.model.repo;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndexRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

	long count();

}
