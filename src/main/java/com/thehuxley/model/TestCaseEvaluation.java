package com.thehuxley.model;

public class TestCaseEvaluation {
    		
    private Long testCaseId;
    
    private String errorMsg;
    
    private String diff;
    
    private Evaluation evaluation;
    
    private Double executionTime = -1D;
    
    public TestCaseEvaluation() {
    	super();
    }

	public TestCaseEvaluation(Long testCaseId) {
		super();
		this.testCaseId = testCaseId;
	}

	public Long getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(Long testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public Double getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Double executionTime) {
		this.executionTime = executionTime;
	}
    
}
