package com.thehuxley.oracle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehuxley.model.Submission;
import com.thehuxley.util.JsonUtils;

public class OracleListener implements MessageListener {
	
	private static Logger logger = LoggerFactory.getLogger(OracleListener.class);
	
	private AmqpTemplate amqpTemplate;

	private String tempDir;
	
	private String scriptDir;
	
    public OracleListener() {
        logger.info("Oracle is listening. Waiting for submissions!");
    }
	
	@Override
	public void onMessage(Message message) {
		//FIXME Refactoring para usar o converter corretamente
//		Map<String, Object> map = (Map<String, Object>) this.converter.fromMessage(message);

        JsonNode rootNode = null;
		
        try {
			rootNode = new ObjectMapper().readTree(new String(message.getBody(), "UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        
		String hash = rootNode.path("hash").asText();

		logger.debug("Processing message. [hash = " + hash + "]");
		
        String input = rootNode.path("input").asText();
        
        List<Submission> submissions = new ArrayList<Submission>();
        
        Iterator<JsonNode> iterator = rootNode.path("submissions").elements();
        while (iterator.hasNext()) {
        	JsonNode node = iterator.next();
        	
        	Submission submission = (Submission) JsonUtils.fromJson(node.toString(), Submission.class);
        	
        	submissions.add(submission);
        }
        
        Oracle oracle = new Oracle(this.scriptDir, this.tempDir);
        
        OracleResult oracleResult = null;
        try {
        	oracleResult = oracle.run(input, submissions);
		} catch (Exception ex) {
			logger.error("unexpected-err", ex);
			oracleResult = new OracleResult(OracleResult.Type.NO_ANSWER, 0, 0, "");
		} finally {
        	oracle.close();
        }
                
        Map<String, Object> result = new HashMap<String, Object>();
		result.put("hash", hash);
        result.put("output", oracleResult.getOutput());
        
        this.amqpTemplate.convertAndSend("oracle_result_queue", result);
        
        logger.debug("Processing message done. [hash = " + hash + "]");
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

}
