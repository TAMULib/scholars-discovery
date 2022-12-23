package edu.tamu.scholars.middleware.view.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.tamu.scholars.middleware.model.GroupOp;

@ExtendWith(SpringExtension.class)
public class FilterGroupTest {

    @Test
    public void testDefaultConstructor() {
        FilterGroup filterGroup = new FilterGroup();
        assertNotNull(filterGroup);
    }

    @Test
    public void testGettersAndSetters() {
        FilterGroup filterGroup = new FilterGroup();

        filterGroup.setA("class");
        filterGroup.setB("type");
        filterGroup.setOpKey(GroupOp.AND);

        assertEquals("class", filterGroup.getA());
        assertEquals("type", filterGroup.getB());
        assertEquals(GroupOp.AND, filterGroup.getOpKey());
    }

}
