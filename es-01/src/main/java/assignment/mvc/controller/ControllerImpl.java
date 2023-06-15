package assignment.mvc.controller;

import assignment.mvc.actors.ReportConfiguration;
import assignment.mvc.actors.ReportConfiguration;
import assignment.mvc.view.View;
import assignment.mvc.view.View;

import java.nio.file.Path;

public class ControllerImpl implements Controller {
    private View view;
    private ReportConfiguration reportConfiguration;
    private AlgorithmStatus algorithmStatus = AlgorithmStatus.IDLE;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void startAlgorithm(Path path, int topN, int nOfIntervals, int maxL) {
        this.reportConfiguration = new ReportConfiguration(topN, nOfIntervals, maxL);

        this.algorithmStatus = AlgorithmStatus.RUNNING;
        this.view.updateAlgorithmStatus(this.algorithmStatus);

    }

    private void registerModelListeners() {

    }

    @Override
    public void stopAlgorithm() {
        if (this.algorithmStatus == AlgorithmStatus.RUNNING) {
            this.algorithmStatus = AlgorithmStatus.STOPPED;

            this.view.updateAlgorithmStatus(this.algorithmStatus);
        }
    }
}
