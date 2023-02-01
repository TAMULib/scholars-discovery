package edu.tamu.scholars.middleware.discovery.model.repo;

import java.io.Serializable;
import java.time.Duration;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface IndexCrudRepository<T, ID extends Serializable> extends IndexRepository<T, ID>, PagingAndSortingRepository<T, ID> {

	<S extends T> S save(S entity, Duration commitWithin);

	<S extends T> Iterable<S> saveAll(Iterable<S> entities, Duration commitWithin);
}
