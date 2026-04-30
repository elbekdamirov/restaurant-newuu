package restaurant.app;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RestaurantFxApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        primaryStage.setTitle("Restaurant Management System");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}