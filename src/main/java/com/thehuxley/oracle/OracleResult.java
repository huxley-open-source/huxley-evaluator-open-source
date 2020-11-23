package com.thehuxley.oracle;

import com.thehuxley.util.JsonUtils;

public class OracleResult {
	
    public enum Type  {
        CONSENSUS,
        MAJORITY,
        INCONCLUSIVE,
        NO_ANSWER
    };

    private Type type;

    private int favour;

    private int against;

    private String output;

    public OracleResult(Type type, int favour, int against, String output) {
        this.type = type;
        this.favour = favour;
        this.against = against;
        this.output = output;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getFavour() {
        return favour;
    }

    public void setFavour(int favour) {
        this.favour = favour;
    }

    public int getAgainst() {
        return against;
    }

    public void setAgainst(int against) {
        this.against = against;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }

}
