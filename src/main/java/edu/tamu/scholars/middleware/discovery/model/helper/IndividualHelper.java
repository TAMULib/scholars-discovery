package edu.tamu.scholars.middleware.discovery.model.helper;

import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.Organization;
import edu.tamu.scholars.middleware.discovery.model.Person;

/**
 * Helper class to get individual label.
 */
public class IndividualHelper {

    public final Individual individual;

    public IndividualHelper(Individual individual) {
        this.individual = individual;
    }

    public String getLabel() {
        ContentMapper cm = ContentMapper.from(individual);
        StringBuilder label = new StringBuilder();

        String clazz = individual.getClazz();

        if (clazz.equals(Organization.class.getSimpleName())) {
            label.append(cm.getValue("name"))
                .append("_");
        } else if (clazz.equals(Person.class.getSimpleName())) {
             label.append(cm.getValue("lastName"))
                .append("_")
                .append(cm.getValue("firstName"))
                .append("_");
        } 

        return label
            .append(individual.getId())
            .toString();
    }

    public static IndividualHelper as(Individual individual) {
        return new IndividualHelper(individual);
    }

}
