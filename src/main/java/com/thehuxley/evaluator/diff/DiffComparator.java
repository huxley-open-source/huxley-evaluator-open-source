package com.thehuxley.evaluator.diff;

/**
 * Classe responsável por fazer a comparação dos arquivos de saída.
 *
 * @author rodrigo
 */
public class DiffComparator {

    /* Mostra no máximo essa quantidade de linhas diferentes no diff.
    Depois ele corta, para evitar que programas que gerem muita saída
    poluam o diff.
     */
    private static final int MAX_LINES = 70;
    private static final int CONTEXT_LINES_DIFF = 3;

    private final HuxleyDiff huxleyDiff;

    private String actualOutputFile;
    private final String expectedOutputFileName;

    /**
     * @param expectedOutputFile Arquivo de saída esperado
     * @param actualOutputFile   Arquivo que realmente ocorreu
     */
    public DiffComparator(String expectedOutputFile, String actualOutputFile) {
        this.actualOutputFile = actualOutputFile;
        this.expectedOutputFileName = expectedOutputFile;
        this.huxleyDiff = new HuxleyDiff();
    }

    public Diff matchIgnoreWhite() throws DiffComparatorException {
        return huxleyDiff.match(expectedOutputFileName, actualOutputFile, MAX_LINES, CONTEXT_LINES_DIFF, true);
    }

    public Diff match() throws DiffComparatorException {
        return huxleyDiff.match(expectedOutputFileName, actualOutputFile, MAX_LINES, CONTEXT_LINES_DIFF, false);
    }

}