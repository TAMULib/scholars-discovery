package edu.tamu.scholars.middleware.discovery.dto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoDataNetwork {
	
	private String name;
	
	private final Map<String, Integer> linkCounts;
	
	private final Map<String, Integer> yearCounts;

	private final Map<DirectedData, Integer> map;

	public CoDataNetwork() {
		linkCounts = new HashMap<>();
		yearCounts = new HashMap<>();
		map = new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public Map<String, Integer> getLinkCounts() {
		return linkCounts.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public Map<String, Integer> getYearCounts() {
		return yearCounts.entrySet().stream()
			.sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public List<DirectedData> getMap() {
		return map.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.map(entry -> entry.getKey().total(entry.getValue()))
			.collect(Collectors.toList());
	}

	public CoDataNetwork countLink(String value) {
		Integer count = linkCounts.containsKey(value) ? linkCounts.get(value) : 0;
		linkCounts.put(value, ++count);

		return this;
	}

	public CoDataNetwork countYear(String year) {
		Integer count = yearCounts.containsKey(year) ? yearCounts.get(year) : 0;
		yearCounts.put(year, ++count);

		return this;
	}

	public CoDataNetwork mapCoAuthor(DirectedData coAuthor) {
		Integer count = map.containsKey(coAuthor) ? map.get(coAuthor) : 0;
		map.put(coAuthor, ++count);

		return this;
	}
	
	public CoDataNetwork to(String name) {
		this.name = name;

		return this;
	}

}
