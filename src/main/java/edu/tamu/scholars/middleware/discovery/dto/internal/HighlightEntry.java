package edu.tamu.scholars.middleware.discovery.dto.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class HighlightEntry<T> {

	private final T entity;

	private final List<Highlight> highlights = new ArrayList<>(1);

	public HighlightEntry(T entity) {
		Assert.notNull(entity, "Entity must not be null!");
		this.entity = entity;
	}

	public T getEntity() {
		return this.entity;
	}

	public List<Highlight> getHighlights() {
		return Collections.unmodifiableList(this.highlights);
	}

	public static class Highlight {

		private final Field field;
		private final List<String> snipplets;

		Highlight(Field field, @Nullable List<String> snipplets) {
			Assert.notNull(field, "Field must not be null!");

			this.field = field;
			this.snipplets = snipplets != null ? snipplets : Collections.emptyList();
		}

		public Field getField() {
			return this.field;
		}

		public List<String> getSnipplets() {
			return this.snipplets;
		}

	}

}
