package assignment.mvc.view;


import assignment.Domain.*;
import assignment.mvc.controller.AlgorithmStatus;
import assignment.mvc.controller.Controller;
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
