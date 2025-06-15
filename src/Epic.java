import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class Epic extends Task {

    private HashMap<Integer, Subtask> subtasksMapInEpic = new HashMap<>();

    public Epic(String name) {
        super(name);
    }

    public Epic(String name, String description) {
        super(name, description);
        this.setStartTime(null);
        this.setDuration(Duration.ZERO);
    }

    public void calculateTimesEpic() {
        if (subtasksMapInEpic.isEmpty()) {
            this.setStartTime(null);
            this.setDuration(Duration.ZERO);
        } else {
            LocalDateTime newEpicStartTime = null;
            Duration sumOfSubtaskDurations = Duration.ZERO;
            Optional<LocalDateTime> minStartTimeOptional = subtasksMapInEpic.values().stream()
                    .map(Subtask::getStartTime)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .min(LocalDateTime::compareTo);
            newEpicStartTime = minStartTimeOptional.orElse(null);

            sumOfSubtaskDurations = subtasksMapInEpic.values().stream()
                    .map(Subtask::getDuration)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(Duration.ZERO, Duration::plus);
            this.setStartTime(newEpicStartTime);
            this.setDuration(sumOfSubtaskDurations);
        }
    }

    public HashMap<Integer, Subtask> getSubtasksMapInEpic() {
        return subtasksMapInEpic;
    }

    public void setSubtasksMapInEpic(HashMap<Integer, Subtask> subtasksMapInEpic) {
        this.subtasksMapInEpic = subtasksMapInEpic;
        calculateTimesEpic();
    }
}