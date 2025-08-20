package edu.tamu.scholars.middleware.discovery.response;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Custom discovery page implementation that implements Page<T> interface.
 */
@JsonPropertyOrder({"content", "page"})
public class DiscoveryPage<T> implements Page<T> {

    private final List<T> content;
    private final Pageable pageable;
    private final long total;

    public DiscoveryPage(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    public DiscoveryPage(Page<T> page) {
        this.content = page.getContent();
        this.pageable = page.getPageable();
        this.total = page.getTotalElements();
    }

    @Override
    @JsonProperty("content")
    public List<T> getContent() {
        return content;
    }

    @JsonProperty("page")
    public PageInfo getPage() {
        return PageInfo.from(getSize(), getTotalElements(), getTotalPages(), getNumber());
    }

    @Override
    @JsonIgnore
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    @JsonIgnore
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    @JsonIgnore
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    @JsonIgnore
    public long getTotalElements() {
        return total;
    }

    @Override
    @JsonIgnore
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
    }

    @Override
    @JsonIgnore
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    @JsonIgnore
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    @JsonIgnore
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    @JsonIgnore
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    @JsonIgnore
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    @Override
    @JsonIgnore
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    @JsonIgnore
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    @JsonIgnore
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : Pageable.unpaged();
    }

    @Override
    @JsonIgnore
    public Pageable previousPageable() {
        return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
    }

    @Override
    @JsonIgnore
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new DiscoveryPage<>(getConvertedContent(converter), pageable, total);
    }

    @Override
    @JsonIgnore
    public Iterator<T> iterator() {
        return content.iterator();
    }

    public static <T> DiscoveryPage<T> from(List<T> content, Pageable pageable, long total) {
        return new DiscoveryPage<>(content, pageable, total);
    }

    public static <T> DiscoveryPage<T> from(Page<T> page) {
        return new DiscoveryPage<>(page);
    }

    private <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {
        return content.stream()
            .map(converter)
            .collect(java.util.stream.Collectors.toList());
    }

    public static class PageInfo {

        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final int number;

        public PageInfo(int size, long totalElements, int totalPages, int number) {
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.number = number;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getNumber() {
            return number;
        }

        public static PageInfo from(int size, long totalElements, int totalPages, int number) {
            return new PageInfo(size, totalElements, totalPages, number);
        }
    }

}
