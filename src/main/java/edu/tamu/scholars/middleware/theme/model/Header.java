package edu.tamu.scholars.middleware.theme.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@Embeddable
public class Header {

    @Embedded
    private Navbar navbar;

    @Embedded
    private Banner banner;

    @ElementCollection
    @CollectionTable(name = "theme_header_variables")
    private List<Style> variables;

    public Header() {
        super();
        this.navbar = new Navbar();
        this.banner = new Banner();
        this.variables = new ArrayList<Style>();
    }

    public Navbar getNavbar() {
        return navbar;
    }

    public void setNavbar(Navbar navbar) {
        this.navbar = navbar;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public List<Style> getVariables() {
        return variables;
    }

    public void setVariables(List<Style> variables) {
        this.variables = variables;
    }

}
