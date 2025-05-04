import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Epic extends Task {

    private LocalDateTime epicStartTime;
    private LocalDateTime epicEndTime;
    private Duration epicDuration = Duration.ZERO;

    public Epic(String name) {
        super(name);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    public void calculateTimesEpic() {
        if (subtasksMapInEpic.isEmpty()) {
            epicStartTime = null;
            epicEndTime = null;
            epicDuration = Duration.ZERO;
        } else {
            LocalDateTime minStartTime = null;
            LocalDateTime maxEndTime = null;
            Duration totalDuration = Duration.ZERO;
            for (Subtask subtask : subtasksMapInEpic.values()) {
                if (subtask.getEndTime().isPresent()) {
                    if (maxEndTime == null || subtask.getEndTime().get().isAfter(maxEndTime)) {
                        maxEndTime = subtask.getEndTime().get();
                    }
                }
                if (subtask.getDuration().isPresent()) {
                    totalDuration = totalDuration.plus(subtask.getDuration().get());
                }
                if (subtask.getStartTime().isPresent()) {
                    if (minStartTime == null || subtask.getStartTime().get().isBefore(minStartTime)) {
                        minStartTime = subtask.getStartTime().get();
                    }
                }
            }
            epicStartTime = minStartTime;
            epicEndTime = maxEndTime;
            epicDuration = totalDuration;
        }
    }


    public LocalDateTime getEpicStartTime() {
        return epicStartTime;
    }

    public LocalDateTime getEpicEndTime() {
        return epicEndTime;
    }

    public Duration getEpicDuration() {
        return epicDuration;
    }

    public HashMap<Integer, Subtask> getSubtasksMapInEpic() {
        return subtasksMapInEpic;
    }

    public void setSubtasksMapInEpic(HashMap<Integer, Subtask> subtasksMapInEpic) {
        this.subtasksMapInEpic = subtasksMapInEpic;
    }

    private HashMap<Integer, Subtask> subtasksMapInEpic = new HashMap<>();


}
