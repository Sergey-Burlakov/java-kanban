import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
    private String name;
    private String description;
    private Status status = Status.NEW;
    private int id;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }


    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Optional<LocalDateTime> getEndTime() {
        if (duration == null || startTime == null) {
            return Optional.empty();
        } else {
            return Optional.of(startTime.plus(duration));
        }
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Optional<Duration> getDuration() {
        if (duration == null) {
            return Optional.empty();
        } else {
            return Optional.of(duration);
        }
    }

    public Optional<LocalDateTime> getStartTime() {
        if (startTime == null) {
            return Optional.empty();
        } else {
            return Optional.of(startTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String result = "Task{" +
                "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length=" + description.length() + '\'' +
                    ", status=" + status +
                    ", id=" + id;
        } else {
            result = result + ", description=null'" + '\'' +
                    ", status=" + status +
                    ", id=" + id;
        }
        return result + "}";
    }
}
