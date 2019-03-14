package org.deidentifier.arx.benchmark;

public class BenchmarkErrorException extends RuntimeException {

    /** SVUID*/
    private static final long serialVersionUID = 3712701021700677407L;

    public BenchmarkErrorException(Exception e) {
        super(e);
    }


}
