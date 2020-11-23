package com.thehuxley.evaluator.diff;

import com.thehuxley.util.JsonUtils;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Diff {

    /*
    Lista de linhas que são diferentes.
    Só terá algum conteúdo se hasMatched() == false
     */
    private List<DiffLine> lines = new ArrayList<>();

    private int totalLines;

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    /*
        Se hasError==true, então houve algum erro no processamento do diff.
        Por exemplo, erro de leitura ao arquivo.
         */
    private boolean hasError = false;
    /*
    Caso hasError==true, o stackTrace indica a causa.
     */
    private String stackTrace = "";


    public void setErrorState(String msg){
        stackTrace = msg;
        hasError = true;
    }



//    -------------------------------------------------------------------
//     Daqui pra baixo estão os métodos para transformar em JSON
//    -------------------------------------------------------------------
    public boolean isMatch() {
        return !hasError && lines.size()==0;
    }


    public List<DiffLine> getLines() {
        return lines;
    }

    public void setLines(List<DiffLine> lines) {
        this.lines = lines;
    }

    public boolean getHasError() {
        return  hasError;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }

    public static void main(String args[]) {
        Diff d = new Diff();
        //d.add(1, " { #\" } { ","  } { ");
        System.out.println(d.toJson());
    }
}


