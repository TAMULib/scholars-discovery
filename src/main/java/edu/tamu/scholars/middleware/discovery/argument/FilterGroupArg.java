package edu.tamu.scholars.middleware.discovery.argument;

import java.util.Optional;

import edu.tamu.scholars.middleware.model.GroupOp;

public class FilterGroupArg {
 
    private final String a;

    private final String b;

    private final GroupOp opKey;

    FilterGroupArg(String a, String b, GroupOp opKey) {
        this.a = a;
        this.b = b;
        this.opKey = opKey;
    }

    public String getB() {
        return b;
    }

    public GroupOp getExpOp() {
        return opKey;
    }

    public String getA() {
        return a;
    }

    public static FilterGroupArg of(String aParam, String bParam, Optional<String> opKey) {
        GroupOp opKeyParam = opKey.isPresent() ? GroupOp.valueOf(opKey.get()) : GroupOp.AND;
        return new FilterGroupArg(aParam, bParam, opKeyParam);
    }

}
