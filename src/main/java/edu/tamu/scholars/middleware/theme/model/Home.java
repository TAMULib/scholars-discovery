package edu.tamu.scholars.middleware.theme.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@Embeddable
public class Home {

    @Column(nullable = false)
    private boolean heroesNavigable;

    @ElementCollection
    private List<Hero> heroes;

    @ElementCollection
    @CollectionTable(name = "theme_home_variables")
    private List<Style> variables;

    public Home() {
        super();
        this.heroesNavigable = false;
        this.heroes = new ArrayList<Hero>();
        this.variables = new ArrayList<Style>();
    }

    public boolean isHeroesNavigable() {
        return heroesNavigable;
    }

    public void setHeroesNavigable(boolean heroesNavigable) {
        this.heroesNavigable = heroesNavigable;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public void setHeroes(List<Hero> heroes) {
        this.heroes = heroes;
    }

    public List<Style> getVariables() {
        return variables;
    }

    public void setVariables(List<Style> variables) {
        this.variables = variables;
    }

}
