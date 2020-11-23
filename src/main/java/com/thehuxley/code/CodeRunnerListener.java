package com.thehuxley.code;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CodeRunnerListener implements MessageListener {
	
	private static Logger logger = LoggerFactory.getLogger(CodeRunnerListener.class);
	
	private AmqpTemplate amqpTemplate;

	private String tempDir;
	
	private String scriptDir;

    public CodeRunnerListener() {
        logger.info("Code runner is listening the queue. Waiting for source codes!");
    }
    
	@Override
	public void onMessage(Message message) {
		JsonNode rootNode = null;
		 
        try {
			rootNode = new ObjectMapper().readTree(new String(message.getBody(), "UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}        
		
		String hash = rootNode.path("hash").asText();
        String input = rootNode.path("input").asText();
        String filename = rootNode.path("filename").asText();
        String sourceCode = rootNode.path("sourceCode").asText();
        String script = rootNode.path("language").asText();
        
        logger.debug("Processing message. [hash = " + hash + "]");
        
		CodeRunner codeRunner = new CodeRunner(this.scriptDir, this.tempDir);
		
		String output = null;
        try {
        	output = codeRunner.run(script, input, filename, sourceCode);
        } finally {
        	codeRunner.close();
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("hash", hash);
        result.put("output", output);
        
        this.amqpTemplate.convertAndSend("code_result_queue", result);
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
