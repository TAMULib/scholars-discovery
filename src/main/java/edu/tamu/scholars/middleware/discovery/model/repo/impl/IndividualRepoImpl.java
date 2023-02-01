package edu.tamu.scholars.middleware.discovery.model.repo.impl;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import edu.tamu.scholars.middleware.discovery.annotation.CollectionTarget;
import edu.tamu.scholars.middleware.discovery.argument.BoostArg;
import edu.tamu.scholars.middleware.discovery.argument.FacetArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.HighlightArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;
import edu.tamu.scholars.middleware.discovery.dto.DataNetwork;
import edu.tamu.scholars.middleware.discovery.dto.DataNetworkDescriptor;
import edu.tamu.scholars.middleware.discovery.dto.internal.FacetAndHighlightPage;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.SolrDocumentRepo;
import edu.tamu.scholars.middleware.discovery.model.repo.custom.SolrDocumentRepoCustom;
import edu.tamu.scholars.middleware.model.OpKey;
import edu.tamu.scholars.middleware.shared.Cursor;

public class IndividualRepoImpl implements SolrDocumentRepoCustom<Individual>, SolrDocumentRepo<Individual> {
    
    private static final Logger logger = LoggerFactory.getLogger(IndividualRepoImpl.class);

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\[(.*?) TO (.*?)\\]$");

    @Value("${spring.data.solr.parser:edismax}")
    private String defType;

    @Value("${spring.data.solr.operator:AND}")
    private String defaultOperator;

    @Autowired
    private SolrClient solrClient;

	@Override
	public <S extends Individual> S save(S entity, Duration commitWithin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> Iterable<S> saveAll(Iterable<S> entities, Duration commitWithin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Individual> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Individual> findAll(Sort sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Individual> findAllById(Iterable<String> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> List<S> saveAll(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <S extends Individual> S saveAndFlush(S entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> List<S> saveAllAndFlush(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllInBatch(Iterable<Individual> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<String> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllInBatch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Individual getOne(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Individual getById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> List<S> findAll(Example<S> example) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> List<S> findAll(Example<S> example, Sort sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<Individual> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Individual> findById(String id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean existsById(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteById(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllById(Iterable<? extends String> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Iterable<? extends Individual> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <S extends Individual> Optional<S> findOne(Example<S> example) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public <S extends Individual> Page<S> findAll(Example<S> example, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> long count(Example<S> example) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <S extends Individual> boolean exists(Example<S> example) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <S extends Individual, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Individual> S save(S document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Individual document) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Individual> findByType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Individual> findByIdIn(List<String> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Individual> findBySyncIds(String syncId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Individual> findBySyncIdsIn(List<String> syncIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public DataNetwork getDataNetwork(DataNetworkDescriptor dataNetworkDescriptor) {
        final String id = dataNetworkDescriptor.getId();
        final DataNetwork dataNetwork = DataNetwork.to(id);

        try {
            final SolrParams queryParams = dataNetworkDescriptor.getSolrParams();

            final QueryResponse response = solrClient.query(collection(), queryParams);

            final SolrDocumentList documents = response.getResults();

            final String dateField = dataNetworkDescriptor.getDateField();

            for (SolrDocument document : documents) {
                if (document.containsKey(dateField)) {
                    Date publicationDate = ((Date) document.getFieldValue(dateField));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(publicationDate);
                    dataNetwork.countYear(String.valueOf(calendar.get(Calendar.YEAR)));
                }
                List<String> values = getValues(document, dataNetworkDescriptor.getDataFields());

                String iid = (String) document.getFieldValue(ID);

                for (String v1 : values) {
                    dataNetwork.index(v1);

                    if (!v1.endsWith(id)) {
                        dataNetwork.countLink(v1);
                    }
                    for (String v2 : values) {
                        // prefer id as source
                        if (v2.endsWith(id)) {
                            dataNetwork.map(iid, v2, v1);
                        } else {
                            dataNetwork.map(iid, v1, v2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to build data network!", e);
        }

        return dataNetwork;
    }

    private List<String> getValues(SolrDocument document, List<String> dataFields) {
        return dataFields.stream()
            .filter(v -> document.containsKey(v))
            .flatMap(v -> document.getFieldValues(v).stream())
            .map(v -> (String) v)
            .collect(Collectors.toList());
    }

    @Override
    public long count(String query, List<FilterArg> filters) {
        return 0;
    }

    @Override
    public List<Individual> findByType(String type, List<FilterArg> filters) {
        filters.add(FilterArg.of(TYPE, Optional.of(type), Optional.of(OpKey.EQUALS.getKey()), Optional.empty()));
        return findAll(filters);
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit) {
        return findMostRecentlyUpdate(limit, new ArrayList<FilterArg>());
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters) {
        return new ArrayList<>();
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters) {
    	return new ArrayList<>();
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters, Sort sort) {
    	return new ArrayList<>();
    }

    @Override
    public Page<Individual> findAll(List<FilterArg> filters, Pageable page) {
    	return new PageImpl<>(new ArrayList<>());
    }

    @Override
    public FacetAndHighlightPage<Individual> search(QueryArg query, List<FacetArg> facets, List<FilterArg> filters, List<BoostArg> boosts, HighlightArg highlight, Pageable page) {
        return null;
    }

    @Override
    public Cursor<Individual> stream(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort) {
    	return null;
    }

    public String collection() {
        return type().getAnnotation(CollectionTarget.class).collection();
    }

    @Override
    public Class<Individual> type() {
        return Individual.class;
    }

}
