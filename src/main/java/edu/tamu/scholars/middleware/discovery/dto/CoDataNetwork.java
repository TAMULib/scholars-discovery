package edu.tamu.scholars.middleware.discovery.dto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoDataNetwork {
	
	private String name;
	
	private final Map<String, Integer> map;
	
	private final Map<DirectedData, Integer> data;

	public CoDataNetwork() {
		map = new HashMap<>();
		data = new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public Map<String, Integer> getMap() {
		return map.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	public List<DirectedData> getData() {
		return data.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.map(entry -> entry.getKey().total(entry.getValue()))
			.collect(Collectors.toList());
	}
	
	public CoDataNetwork addCoAuthor(String coAuthor) {
		Integer count = map.containsKey(coAuthor) ? map.get(coAuthor) : 0;
		map.put(coAuthor, ++count);

		return this;
	}
	
	public CoDataNetwork addCoAuthor(DirectedData coAuthor) {
		Integer count = data.containsKey(coAuthor) ? data.get(coAuthor) : 0;
		data.put(coAuthor, ++count);

		return this;
	}
	
	public CoDataNetwork to(String name) {
		this.name = name;

		return this;
	}

}
