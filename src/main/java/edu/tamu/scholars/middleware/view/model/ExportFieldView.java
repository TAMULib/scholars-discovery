package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "export_fields")
@AttributeOverride(name = "name", column = @Column(nullable = false))
public class ExportFieldView extends FieldView {

    private static final long serialVersionUID = -5336237302881223760L;

    @Column(name = "\"limit\"", nullable = false)
    private int limit;

    public ExportFieldView() {
        super();
        limit = Integer.MAX_VALUE;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
