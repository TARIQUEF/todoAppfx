package todo;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class MyToDoApp extends Application {

    private ListView<HBox> listView; // listview of hboxes (bullet + label desc)
    private TaskDAO taskDAO;  // interact with database

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        taskDAO = new TaskDAO(); // get methods

        primaryStage.setTitle("To-Do List");

        // listview instance and aesthetic pink w beige borders
        listView = new ListView<>();
        listView.getStyleClass().add("listview-background");

        // user write box set to taskInput
        TextField taskInput = new TextField(); // don't put writing inside it makes it opaque but do prompt for ghost text
        taskInput.setPromptText("Enter a new task");

        // action buttons with event handling into methods
        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> addTask(taskInput.getText())); // gotta fetch the text otherwise it's a NODE

        Button clearButton = new Button("Clear Task");
        clearButton.setOnAction(e -> deleteSelectedTask());

        Button completeButton = new Button("Complete Task");
        completeButton.setOnAction(e -> completeSelectedTask());

        // put textfield and the add and clear buttons next to each other with 10 spaces between
        HBox inputBox = new HBox(10, taskInput, addButton, clearButton);
        inputBox.setAlignment(Pos.CENTER); // centered

        // keep at bottom there's not a lot of space
        HBox completeBox = new HBox(completeButton);
        completeBox.setAlignment(Pos.CENTER);

        // my three top cute images held in image vars
        Image coffeeImage = loadImage("/drinkPic.png");
        Image topImage = loadImage("/titlePIC.png");
        Image clipImage = loadImage("/clip.png");

        // make imageviews for the three top images so we can actually see and work with them
        ImageView coffeeImageView = new ImageView(coffeeImage);
        coffeeImageView.setFitHeight(50); // smaller
        coffeeImageView.setPreserveRatio(true); // to make it not wonky

        ImageView topImageView = new ImageView(topImage);
        topImageView.setFitHeight(50);
        topImageView.setPreserveRatio(true);

        ImageView clipImageView = new ImageView(clipImage);
        clipImageView.setFitHeight(50);
        clipImageView.setPreserveRatio(true);

        // putting top images together with a horizontal layout
        HBox imageBox = new HBox(10, coffeeImageView, topImageView, clipImageView);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle("-fx-padding: 10;");

        // a vertical layout just for the inputs (add and delete) and the complete right under
        VBox bottomBox = new VBox(10, inputBox, completeBox);
        bottomBox.setAlignment(Pos.CENTER);

        // main ROOT layout putting everything together easily with borderpane
        BorderPane root = new BorderPane();
        root.setTop(imageBox); // at the top is cute images and title image
        root.setCenter(listView); // in the middle is our listview (filled with box of tasks)
        root.setBottom(bottomBox); // bottom control box coupled with vbox previously

        // scene!!! and aesthetics duh
        Scene scene = new Scene(root, 370, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // set scene and... action
        primaryStage.setScene(scene);
        primaryStage.show();

        loadTasks(); // after setting up the stuff we want to actually load in all our saved data!
    }

    // to add a task
    private void addTask(String taskDescription) { // input text of textField above
        if (taskDescription != null && !taskDescription.trim().isEmpty()) { // takes off ws makes sure something actually here
            // make task, default set to 0 but in sql we do auto_increment primary key so it'll change in that
            Task task = new Task(0, taskDescription, false);
            taskDAO.saveTask(task); // saved to database in that class going to be reformatted with sql

            // image view of our bullet point and set size
            ImageView bulletImageView = new ImageView(loadImage("/cuteBullet.png"));
            bulletImageView.setFitHeight(20);
            bulletImageView.setFitWidth(20);

            // label with the string to actually show it on screen and aesthetic
            Label taskLabel = new Label(taskDescription);
            taskLabel.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, FontPosture.ITALIC, 15));

            // combine bullet and task label together horizontally layout
            HBox taskBox = new HBox(10, bulletImageView, taskLabel);
            taskBox.setAlignment(Pos.CENTER_LEFT);
            taskBox.setUserData(task);  // a way to attach task object to this hbox for easy access

            listView.getItems().add(taskBox); // putting in listview which holds hboxes
        }
    }

    // delete method called
    private void deleteSelectedTask() {
        // picking out the specific hbox item that was selected
        HBox selectedTaskBox = listView.getSelectionModel().getSelectedItem();
        if (selectedTaskBox != null) {
            Task task = (Task) selectedTaskBox.getUserData();  // get the task object that was stuck on with add method

            // calls delete method in database
            taskDAO.deleteTask(task.getId());

            // removes in UI from listview
            listView.getItems().remove(selectedTaskBox);
        }
    }

    // complete method
    private void completeSelectedTask() {
        HBox selectedTaskBox = listView.getSelectionModel().getSelectedItem(); // saves hbox of selected
        if (selectedTaskBox != null) {
            // gets the label hbox
            Label taskLabel = (Label) selectedTaskBox.getChildren().get(1);
            Task task = (Task) selectedTaskBox.getUserData(); // gets task object

            // update in database set completed and update database
            task.setCompleted(true);
            taskDAO.updateTask(task);

            // mark completed in ui
            markAsCompleted(taskLabel);
        }
    }

    // makes it lighter color
    private void markAsCompleted(Label taskLabel) {
        taskLabel.setTextFill(Color.GRAY); // Gray out the text
    }

    // makes image object out of path from resource check stackoverflow for help
    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Error loading " + path);
            return null;
        }
        return new Image(stream);
    }

    // load in previous tasks from database
    private void loadTasks() {
        // gets a list of tasks returned from database method task loaded
        List<Task> tasks = taskDAO.loadTasks();

        // for each task load up an image instance of the bullet using loadImage
        for (Task task : tasks) {
            ImageView bulletImageView = new ImageView(loadImage("/cuteBullet.png"));
            bulletImageView.setFitHeight(20);
            bulletImageView.setFitWidth(20);

            // make label with task.getDesc()
            Label taskLabel = new Label(task.getDescription());
            taskLabel.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, FontPosture.ITALIC, 16));

            // makes an hbox with bullet and label text
            HBox taskBox = new HBox(10, bulletImageView, taskLabel);
            taskBox.setAlignment(Pos.CENTER_LEFT);
            taskBox.setUserData(task);  // store task object in hbox

            // if it's completed mark it as so in UI
            if (task.isCompleted()) {
                markAsCompleted(taskLabel);
            }

            // add each taskBox to listview which is empty on startup but will be filled with database tasks stored
            listView.getItems().add(taskBox);
        }
    }
}
