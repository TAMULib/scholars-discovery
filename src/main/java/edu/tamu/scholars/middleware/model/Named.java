package edu.tamu.scholars.middleware.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 */
@MappedSuperclass
public abstract class Named implements Serializable {

    private static final long serialVersionUID = 945496864869417217L;

    @Id
    @JsonInclude(Include.NON_EMPTY)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Size(min = 2, max = 64, message = "${Named.nameSize}")
    @Column(nullable = false, unique = true)
    protected String name;

    public Named() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull(message = "${Named.nameRequired}")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
