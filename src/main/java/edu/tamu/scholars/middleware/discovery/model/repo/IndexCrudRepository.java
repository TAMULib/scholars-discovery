package edu.tamu.scholars.middleware.discovery.model.repo;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndexCrudRepository<T, ID extends Serializable> extends IndexRepository<T, ID> {

    <S extends T> S save(S entity, int commitWithinMs);

    <S extends T> Iterable<S> saveAll(Iterable<S> entities, int commitWithinMs);

}
