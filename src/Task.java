import java.util.HashMap;
import java.util.Objects;

public class Task {
   protected String name;
   protected String description;
   protected Status status = Status.NEW;
   protected int id;

   public Task(String name) {
      this.id = TaskManager.getNewId();
      this.name = name;
   }

   public Task(String name, String description) {

      this.id = TaskManager.getNewId();
      this.name = name;
      this.description = description;
   }

   public Task(String name, String description, String status){
      this.id = TaskManager.getNewId();
      this.name = name;
      this.description = description;
      this.status = returnStatusString(status);
   }

   public Task(String name, String description, Status status){
      this.id = TaskManager.getNewId();
      this.name = name;
      this.description = description;
      this.status = status;
   }

    Status returnStatusString(String nameStatus) {
      switch (nameStatus) {
         case "новый":
            return Status.NEW;
         case "в процессе":
            return Status.IN_PROGRESS;
         case "выполнен":
            return Status.DONE;
         default: return Status.NEW;
      }
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
              if (description != null){
                 result = result + ", description.length=" + description.length();
              } else {
                 result = result + ", description=null'" + '\'' +
                         ", status=" + status +
                         ", id=" + id ;
              }
              return result + "}";
   }
}
