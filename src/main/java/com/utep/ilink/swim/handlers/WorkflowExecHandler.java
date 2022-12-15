package com.utep.ilink.swim.handlers;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class WorkflowExecHandler {
    private String _filePath;
    private String _command;
    private Logger _logger;

    /**
     * Initialize a workflow execution handler. It will run a CWL workflow
     * 
     * @param filePath Path of the CWL file to be executed
     */
    public WorkflowExecHandler(String filePath) {
        try {
            this._logger = LoggerFactory.getLogger(WorkflowExecHandler.class);

            this._filePath = filePath;
            this._command = "cwl-runner " + this._filePath;

        } catch (Exception e) {
            _logger.error(e.toString());
        }
    }

    public boolean runWorkflow() {
        try {
            Runtime.getRuntime().exec(_command);
        } catch (Exception exception) {
            _logger.error("There was an error while running the CWL file");
            _logger.error(exception.toString());
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        WorkflowExecHandler obj = new WorkflowExecHandler(
                "/Users/ravargasaco/Apps/flask/workflow-serializer/service.cwl");
        System.out.println(obj.runWorkflow());
    }
}