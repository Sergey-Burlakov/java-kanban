import java.util.HashMap;
import java.util.Objects;

public class Task {
   private String name;
   private String description;
   private Status status = Status.NEW;
   private int id;

   public Task(String name) {
      this.name = name;
   }

   public Task(String name, String description) {
      this.name = name;
      this.description = description;
   }


   public Task(String name, String description, Status status){
      this.name = name;
      this.description = description;
      this.status = status;
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
                 result = result + ", description.length=" + description.length()+ '\'' +
                         ", status=" + status +
                         ", id=" + id ;
              } else {
                 result = result + ", description=null'" + '\'' +
                         ", status=" + status +
                         ", id=" + id ;
              }
              return result + "}";
   }
}
