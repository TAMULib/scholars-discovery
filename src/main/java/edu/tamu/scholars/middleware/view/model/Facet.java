package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.data.domain.Sort.Direction;

import edu.tamu.scholars.middleware.model.OpKey;

/**
 * Embeddable domain model for {@link CollectionView} result set faceting.
 */
@Embeddable
@JsonInclude(Include.NON_NULL)
public class Facet {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OpKey opKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FacetType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FacetSort sort;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(nullable = false)
    private int pageSize;

    @Column(nullable = false)
    private int pageNumber;

    @Column(nullable = false)
    private boolean expandable;

    @Column(nullable = false)
    private boolean collapsible;

    @Column(nullable = false)
    private boolean collapsed;

    @Column(nullable = false)
    private boolean useDialog;

    @Column(nullable = false)
    private boolean hidden;

    @Column(nullable = true)
    private String rangeStart;

    @Column(nullable = true)
    private String rangeEnd;

    @Column(nullable = true)
    private String rangeGap;

    /**
     * Default `Facet` constructor.
     * 
     * <table>
     * <tr><td>opKey</td><td>{@link OpKey}.EQUALS</td></tr>
     * <tr><td>type</td><td>{@link FacetType}.STRING</td></tr>
     * <tr><td>sort</td><td>{@link FacetSort}.COUNT</td></tr>
     * <tr><td>direction</td><td>{@link Direction}.DESC</td></tr>
     * <tr><td>pageSize</td><td>10</td></tr>
     * <tr><td>pageNumber</td><td>1</td></tr>
     * <tr><td>expandable</td><td>true</td></tr>
     * <tr><td>collapsible</td><td>true</td></tr>
     * <tr><td>collapsed</td><td>true</td></tr>
     * <tr><td>useDialog</td><td>false</td></tr>
     * <tr><td>hidden</td><td>false</td></tr>
     * </table>
     */
    public Facet() {
        opKey = OpKey.EQUALS;
        type = FacetType.STRING;
        sort = FacetSort.COUNT;
        direction = Direction.DESC;
        pageSize = 10;
        pageNumber = 1;
        expandable = true;
        collapsible = true;
        collapsed = true;
        useDialog = false;
        hidden = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public OpKey getOpKey() {
        return opKey;
    }

    public void setOpKey(OpKey opKey) {
        this.opKey = opKey;
    }

    public FacetType getType() {
        return type;
    }

    public void setType(FacetType type) {
        this.type = type;
    }

    public FacetSort getSort() {
        return sort;
    }

    public void setSort(FacetSort sort) {
        this.sort = sort;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isUseDialog() {
        return useDialog;
    }

    public void setUseDialog(boolean useDialog) {
        this.useDialog = useDialog;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(String rangeStart) {
        this.rangeStart = rangeStart;
    }

    public String getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(String rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public String getRangeGap() {
        return rangeGap;
    }

    public void setRangeGap(String rangeGap) {
        this.rangeGap = rangeGap;
    }

}
