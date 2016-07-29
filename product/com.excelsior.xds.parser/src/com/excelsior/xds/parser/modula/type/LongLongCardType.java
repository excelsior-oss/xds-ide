package com.excelsior.xds.parser.modula.type;

import java.math.BigInteger;

public class LongLongCardType extends NumericalType {

    public LongLongCardType(String debugName) {
        super(debugName, BigInteger.ZERO, new BigInteger("FFFFFFFFFFFFFFFF", 16));
    }
}
