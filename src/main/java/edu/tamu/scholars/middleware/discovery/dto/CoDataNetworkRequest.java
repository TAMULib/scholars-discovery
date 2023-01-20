package edu.tamu.scholars.middleware.discovery.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.data.util.Pair;

public class CoDataNetworkRequest {

	private final String id;

	private final String dateField;

	private final List<String> dataFields;

	private final List<Pair<String, String>> typeFilters;

	public CoDataNetworkRequest(String id, String dateField, List<String> dataFields, List<Pair<String, String>> typeFilters) {
		super();
		this.id = id;
		this.dateField = dateField;
		this.dataFields = dataFields;
		this.typeFilters = typeFilters;
	}

	public String getId() {
		return id;
	}

	public String getDateField() {
		return dateField;
	}

	public List<String> getDataFields() {
		return dataFields;
	}

	public List<Pair<String, String>> getTypeFilters() {
		return typeFilters;
	}

	public String getSort() {
		return String.format("%s asc", dateField);
	}

	public String getFieldList() {
		StringBuilder fl = new StringBuilder();
		fl.append(dateField);
		dataFields.forEach(dataField -> {
			fl.append(",");
			fl.append(dataField);
		});
		return fl.toString();
	}

	public String getFilterQuery() {
		StringBuilder fq = new StringBuilder();
		fq.append("syncIds:");
		fq.append(id);
		typeFilters.forEach(typeFilter -> {
			fq.append(" AND ");
			fq.append(typeFilter.getFirst());
			fq.append(":");
			fq.append(typeFilter.getSecond());
		});
		return fq.toString();
	}
	
	public SolrParams getSolrParams() {
		final Map<String, String> queryParamMap = new HashMap<>();
		queryParamMap.put("q", "*:*");
		queryParamMap.put("rows", String.valueOf(Integer.MAX_VALUE));
		queryParamMap.put("sort", getSort());
		queryParamMap.put("fl", getFieldList());
		queryParamMap.put("fq", getFilterQuery());

		return new MapSolrParams(queryParamMap);
	}

}
