package scripts;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class InputWindowController {

    @FXML
    public TextField pathText;

    @FXML
    public TextField xText;

    @FXML
    public TextField yText;

    @FXML
    public TextField resultText;

    @FXML
    public TextField fromText;

    @FXML
    public TextField toText;

    MainController mainController;
    Stage stage;

    @FXML
    public void sendInputInformation() {
        try {
            String path = pathText.getText();
            int x = Integer.parseInt(xText.getText());
            int y = Integer.parseInt(yText.getText());
            int result = Integer.parseInt(resultText.getText());
            int from = Integer.parseInt(fromText.getText());
            int to = Integer.parseInt(toText.getText());
            boolean response = mainController.setInput(new InputInformation(path, x - 1, y - 1, result - 1, from - 1, to));
            if (response) {
                stage.close();
            }
        } catch (RuntimeException exc) {
            exc.printStackTrace();
        }
    }

    @FXML
    void close() {
        stage.close();
    }
}
