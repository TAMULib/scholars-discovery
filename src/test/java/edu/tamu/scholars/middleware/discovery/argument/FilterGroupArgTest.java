package edu.tamu.scholars.middleware.discovery.argument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.tamu.scholars.middleware.model.GroupOp;

@ExtendWith(SpringExtension.class)
public class FilterGroupArgTest {

    @Test
    public void testDefaultConstructor() {
        FilterGroupArg filterGroupArg = new FilterGroupArg("class", "type", GroupOp.AND);
        assertNotNull(filterGroupArg);
        assertEquals("class", filterGroupArg.getA());
        assertEquals("type", filterGroupArg.getB());
        assertEquals(GroupOp.AND, filterGroupArg.getExpOp());
    }

    @Test
    public void testOfQueryParameter() {
        String opA = "class";
        String opB = "type";
        Optional<String> opKey = Optional.of(GroupOp.OR.getKey());
        FilterGroupArg filterGroupArg = FilterGroupArg.of(opA, opB, opKey);
        assertNotNull(filterGroupArg);
        assertEquals("class", filterGroupArg.getA());
        assertEquals("type", filterGroupArg.getB());
        assertEquals(GroupOp.OR, filterGroupArg.getExpOp());
    }

}
