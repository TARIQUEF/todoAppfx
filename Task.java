package todo;

// A TASK
public class Task {
    private int id; // PRIMARY KEY
    private String description;
    private boolean completed;

    public Task() {
    }

    public Task(int id, String description, boolean completed) { // constructor w the 3 things we need
        this.id = id;
        this.description = description;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
