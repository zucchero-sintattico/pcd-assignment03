package assignment.view;


import assignment.Statistic;
import assignment.actors.Range;
import assignment.controller.AlgorithmStatus;
import assignment.controller.Controller;

import java.util.List;
import java.util.Map;


public interface View {

    void setController(Controller controller);

    void updateAlgorithmStatus(AlgorithmStatus status);

    void updateTopN(List<Statistic> stats);

    void updateDistribution(Map<Range, Integer> distribution);

    void updateNumberOfFiles(int numberOfFiles);

    void start();
}
