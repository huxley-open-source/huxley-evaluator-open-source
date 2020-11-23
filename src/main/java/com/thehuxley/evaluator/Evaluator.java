package com.thehuxley.evaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.thehuxley.runner.TempFilesCreator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thehuxley.evaluator.diff.Diff;
import com.thehuxley.evaluator.diff.DiffComparator;
import com.thehuxley.evaluator.diff.DiffComparatorException;
import com.thehuxley.model.Evaluation;
import com.thehuxley.model.Problem;
import com.thehuxley.model.Submission;
import com.thehuxley.model.TestCaseEvaluation;
import com.thehuxley.runner.SolutionRunner;

public class Evaluator {

    private final int OUTPUT_DELTA = 4 * 1024; // 4kb uma folguina na saída antes de truncar

	private static Logger logger = LoggerFactory.getLogger(Evaluator.class);

    private String tempDir;
    private String testCasesDir;

    private String scriptDir;

    public Evaluator(String scriptDir, String tempDir, String testCasesDir) {
    	super();
        logger.info("New evaluator created.");
    	
		String randomString = null;
		
		do {
			randomString = tempDir + File.separator + UUID.randomUUID().toString();
		} while(new File(randomString).exists() && new File(randomString).isDirectory());

		this.tempDir = randomString;
        this.testCasesDir = testCasesDir;
		new File(this.tempDir).mkdirs();

		this.scriptDir = scriptDir;
    }
    
    private void createSubmissionFile(Submission submission) {
        File distDir = new File(this.tempDir);
        
        if (distDir.exists() || distDir.mkdirs()) {
            File tempFile = Paths.get(this.tempDir.toString(), submission.getFilename()).toFile();
            PrintWriter writer = null;
            
            try {
                writer = new PrintWriter(tempFile);
                writer.write(submission.getSourceCode());
                writer.close();
                
                submission.setSubmissionFile(tempFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<TestCaseEvaluation> evaluate(Submission submission) {
    	logger.info("Running evaluator... [submission.id = " + submission.getId() + "]");
    	
    	createSubmissionFile(submission);
    	
        List<TestCaseEvaluation> testCaseEvaluations = new ArrayList<>();

        TempFilesCreator tempFiles = null;

        try {
            SolutionRunner runner = new SolutionRunner(
                    this.scriptDir,
                    submission.getLanguage().getScript(),
                    submission.getSubmissionFile(),
                    submission.getProblem().getTimeLimit());

            runner.configurePaths();

            Problem problem = submission.getProblem();

            List<Long> testCases = submission.getTestCases();

            tempFiles = new TempFilesCreator(this.tempDir);

            for (Long id : testCases) {

                String inputFileName = Paths.get(
                        this.testCasesDir,
                        submission.getProblem().getId().toString(),
                        id + ".in").toString();

                String outputFileName = Paths.get(
                        this.testCasesDir,
                        submission.getProblem().getId().toString(),
                        id + ".out").toString();

                File inputFile = new File(inputFileName);
                File outputFile = new File(outputFileName);

                if (!inputFile.exists() || !outputFile.exists()) {
                    logger.error("io-file-not-found: " + inputFileName + " / " + outputFileName);
                    continue;
                }

                FileUtils.copyFile(inputFile, new File(tempFiles.getInputFileName()));



                int maxOutputSize = (int) (outputFile.length() * 1.1);

                runner.run(tempFiles.getInputFileName(), maxOutputSize + OUTPUT_DELTA);

                TestCaseEvaluation testCaseEvaluation = new TestCaseEvaluation(id);

                checkExecutionErrors(runner, testCaseEvaluation);
                if (testCaseEvaluation.getEvaluation() == null) { //se tem avaliação, é pq houve erro de execução. resposta não deve ser avaliada.


                    checkAnswer(runner, outputFileName, testCaseEvaluation);
                }

                testCaseEvaluations.add(testCaseEvaluation);

                logger.info("Test case evaluation: " + testCaseEvaluation.getEvaluation());
            }
        } catch (IOException ex) {
            logger.error("unexpected-error", ex);
            throw new RuntimeException(ex);
        } finally {
            try {
				FileUtils.cleanDirectory(new File(this.tempDir));
			} catch (Exception e) {
				logger.error("Não foi possível apagar os arquivos criados.", e);
                throw new RuntimeException(e);
			}
        }
		
		logger.info("Evaluator done.");
		
        return testCaseEvaluations;
    }
    
	public void close() {
		try {
			FileUtils.deleteDirectory(new File(this.tempDir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void checkAnswer(final SolutionRunner solutionRunner, final String expectedOutputFileName, final TestCaseEvaluation testCaseEvaluation) {
		testCaseEvaluation.setExecutionTime(solutionRunner.getExecutionTime());
		
		try {
			if (Files.readAllLines(Paths.get(solutionRunner.getOutputFileName())).size() == 0) {
				testCaseEvaluation.setEvaluation(Evaluation.EMPTY_ANSWER);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
            DiffComparator diffComparator = new DiffComparator(expectedOutputFileName,
            		solutionRunner.getOutputFileName());

            Diff diff = diffComparator.match();

            if (diff.getHasError()) {
                /* Essa situação ocorre quando, por exemplo, a submissão extrapola o
                 tamanho máximo permitido para uma linha
                 */
            	testCaseEvaluation.setErrorMsg(diff.getStackTrace());
            	testCaseEvaluation.setEvaluation(Evaluation.RUNTIME_ERROR);
            } else {
                if (!diff.isMatch()) {

                	testCaseEvaluation.setDiff(diff.toJson());

                    Diff newDiff = diffComparator.matchIgnoreWhite();
                    if (newDiff.isMatch()) {                            
                        testCaseEvaluation.setEvaluation(Evaluation.PRESENTATION_ERROR);
                    } else {
                        testCaseEvaluation.setEvaluation(Evaluation.WRONG_ANSWER);
                    }
                } else {
                    testCaseEvaluation.setEvaluation(Evaluation.CORRECT);
                }
            }
        } catch (DiffComparatorException e) {
        	testCaseEvaluation.setEvaluation(Evaluation.HUXLEY_ERROR);
        }
	}
	
	private void checkExecutionErrors(final SolutionRunner solutionRunner, final TestCaseEvaluation testCaseEvaluation) {
		switch (solutionRunner.getExitVal()) {
			case SolutionRunner.NORMAL_EXECUTION:
				break;
			case SolutionRunner.RUNTIME_ERROR:
				/*
				 * Ainda existe a possibilidade que o erro de execucao tenha sido
				 * causado porque o usuario estourou o tamanho maximo da saida.
				 * Nesse caso, a execucao sera parada pelo Java e portanto, o
				 * resultado vira como se fosse um runtime
				 */
				if (solutionRunner.isOutputFull()) {
					testCaseEvaluation.setEvaluation(Evaluation.WRONG_ANSWER);
				} else {
					testCaseEvaluation.setErrorMsg(solutionRunner.getErrorMessage());
					testCaseEvaluation.setEvaluation(Evaluation.RUNTIME_ERROR);
				}
				
				break;
			case SolutionRunner.TIME_OUT:
				testCaseEvaluation.setEvaluation(Evaluation.TIME_LIMIT_EXCEEDED);
				break;
			case SolutionRunner.COMPILATION_ERROR:
				testCaseEvaluation.setErrorMsg(solutionRunner.getErrorMessage());
				testCaseEvaluation.setEvaluation(Evaluation.COMPILATION_ERROR);
				break;
	
			default:
				testCaseEvaluation.setEvaluation(Evaluation.HUXLEY_ERROR);
		}
	}
	
}