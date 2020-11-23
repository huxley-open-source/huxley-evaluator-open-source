package com.thehuxley.evaluator.diff;

import com.thehuxley.util.BoundedBufferedReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by rodrigo on 01/10/15.
 */
public class HuxleyDiff {

    private static Logger logger = LoggerFactory.getLogger(HuxleyDiff.class);



    public Diff match(String expectedFile, String actualFile, int maxDiff, int delta, boolean ignoreWhite) {


        Diff diff = new Diff();
        ArrayList <DiffLine> buffer = new ArrayList<DiffLine>(1000);
        ArrayList<Integer> diffIndex = new ArrayList<Integer>(100);

        BufferedReader expectedFileReader = null;
        BufferedReader actualFileReader = null;

        try{
            /**
             * Essa classe BoundedBufferedReader resolve o problema caso a linha seja gigante.
             * Ela limita o tamanho da linha a um valor delimitado por uma constante declarada
             * dentro dela. Para alterar, altere essa constante.
             */
            String expectedOutput = FileUtils.readFileToString(new File(expectedFile)).trim();
            expectedFileReader = new BoundedBufferedReader(new StringReader(expectedOutput));
            actualFileReader = new BoundedBufferedReader(new FileReader(actualFile));
            String expectedLine, actualLine;
            int lineCounter = 0;
            boolean areLineEquals;

            expectedLine= expectedFileReader.readLine();
            actualLine= actualFileReader.readLine();
            lineCounter++;

            while( expectedLine != null && actualLine != null) {

                if (ignoreWhite) {
                    /*
                    \s significa um caracter branco. Nesse replace, ele troca todos os caracteres
                    brancos por nada. E então compara as duas strings
                    */
                    areLineEquals = expectedLine.replaceAll("\\s+", "").equals(actualLine.replaceAll("\\s+", ""));
                } else {
                    areLineEquals = expectedLine.equals(actualLine);
                }

                buffer.add(new DiffLine(lineCounter, expectedLine, actualLine, areLineEquals));
                if (!areLineEquals) {
                    diffIndex.add(lineCounter);
                }

                expectedLine = expectedFileReader.readLine();
                actualLine = actualFileReader.readLine();
                lineCounter++;
            }
            /* Caso um dos arquivos tenha acabado primeiro que o outro, o loop abaixo
             * vai até o fim do arquivo mais comprido */
            while (expectedLine!= null || actualLine!=null)
            {
                if (expectedLine!=null) {
                    buffer.add(new DiffLine(lineCounter, expectedLine, "", false));
                    expectedLine = expectedFileReader.readLine();
                } else {
                    buffer.add(new DiffLine(lineCounter, "", actualLine, false));
                    actualLine = actualFileReader.readLine();
                }
                diffIndex.add(lineCounter);
                ++lineCounter;
            }

            if (diffIndex.size() > 0){
                // Houve alguma diferença
                int firstDiffLineNumber = diffIndex.get(0);
                int firstIndex = Math.max(0, firstDiffLineNumber - delta-1);
                int lastIndex = Math.min(firstDiffLineNumber + delta-1, buffer.size()-1);

                // Acha o último índice a ser colocado no array
                for (int i=1; i< diffIndex.size(); ++i) {
                    int currentIndex = diffIndex.get(i) + delta-1;
                    int distance = currentIndex - firstIndex;
                    if ( distance < maxDiff  && currentIndex > lastIndex) {
                        lastIndex = currentIndex;
                    } else {
                        break;
                    }
                }
                lastIndex = Math.min(lastIndex, buffer.size()-1);
                diff.setLines(buffer.subList(firstIndex, lastIndex+1));
            }
            diff.setTotalLines(lineCounter-1);




        }catch(Exception e){
            diff.setErrorState(e.getMessage());
            //logger.error(e.getMessage(), e);
        } finally {
            if (expectedFileReader!=null) {
                try {
                    expectedFileReader.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }

            if (actualFileReader!=null) {
                try {
                    actualFileReader.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return diff;



    }

    public static void main(String args[]) {




    }
}
