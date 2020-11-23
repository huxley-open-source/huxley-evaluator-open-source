package com.thehuxley.evaluator;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;

import com.thehuxley.model.Submission;
import com.thehuxley.model.TestCaseEvaluation;
import com.thehuxley.util.JsonUtils;

public class EvaluatorListener implements MessageListener {
	
	private static Logger logger = LoggerFactory.getLogger(EvaluatorListener.class);

	private static final Executor EXECUTOR = Executors.newFixedThreadPool(8);

	private AmqpTemplate amqpTemplate;

	private String tempDir;
	
	private String scriptDir;

	private String testCaseDir;

    public EvaluatorListener() {
        logger.info("Evaluator is listening the queue. Waiting for submissions!");
    }
	
	@Override
	public void onMessage(Message message) {
		//FIXME Refactoring para usar o converter corretamente
//		Map<String, Object> map = (Map<String, Object>) this.converter.fromMessage(message);

		try {

			final Submission submission = (Submission) JsonUtils.fromJson(new String(message.getBody(), "UTF-8"), Submission.class);
			final Evaluator evaluator = new Evaluator(this.scriptDir, this.tempDir, this.testCaseDir);
		
			EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					try {
						execute(submission, evaluator);
					} catch (Exception ex) {
						logger.error("unexpected-err", ex);
					}
				}
			});
		} catch (UnsupportedEncodingException e) {
			//Not gonna happen
			e.printStackTrace();
		}
	}

	public final void execute(Submission submission, Evaluator evaluator) {
		List<TestCaseEvaluation> testCaseEvaluations = null;
		try {
			testCaseEvaluations = evaluator.evaluate(submission);
		} finally {
			evaluator.close();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("submissionId", submission.getId());
		result.put("isReevaluation", submission.isReevaluation());
		result.put("testCaseEvaluations", testCaseEvaluations);

		final Integer priority = submission.isReevaluation() ? 0 : 10;

		this.amqpTemplate.convertAndSend("evaluation_queue", result, new MessagePostProcessor() {

			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setPriority(priority);
				return message;
			}

		});
	}

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public void setScriptDir(String scriptDir) {
		this.scriptDir = scriptDir;
	}

	public void setTestCaseDir(String testCaseDir) { this.testCaseDir = testCaseDir; }
	
}
