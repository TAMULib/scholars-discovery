package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents a view for display subsections.
 * Extends the {@link FieldView} class.
 */
@Entity
@Table(name = "display_subsections")
@AttributeOverride(name = "name", column = @Column(nullable = false))
public class DisplaySubsectionView extends FieldView {

    private static final long serialVersionUID = 7776446742411477782L;

    @Column(nullable = false)
    private int pageSize;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String template;

    /**
     * Default constructor.
     * Initializes the page size to 5.
     */
    public DisplaySubsectionView() {
        super();
        pageSize = 5;
    }

    /**
     * Gets the page size.
     *
     * @return The page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize The new page size.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets the template.
     *
     * @return The template.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     *
     * @param template The new template.
     */
    public void setTemplate(String template) {
        this.template = template;
    }
}

