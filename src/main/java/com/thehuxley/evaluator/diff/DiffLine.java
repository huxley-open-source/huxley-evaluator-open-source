package com.thehuxley.evaluator.diff;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class DiffLine {
    int number;
    String expected;
    String actual;
    boolean match;


    public DiffLine(int number, String expected, String actual, boolean match) {
        this.number = number;
        this.expected = expected;
        this.actual = actual;
        this.match = match;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }
}