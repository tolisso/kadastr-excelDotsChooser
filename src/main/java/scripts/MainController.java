package scripts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;


public class MainController {

    @FXML
    LineChart<Number, Number> lineChart;

    @FXML
    Slider selectingRadiusSlider;

    private DynamicLineChart dynamicLineChart;
    private double selectionRadius = 20;
    private InputInformation inputInformation;

    void setLineChart() {
        lineChart.setAnimated(false);
        lineChart.getXAxis().setAutoRanging(true);
        lineChart.getYAxis().setAutoRanging(true);
        dynamicLineChart = new DynamicLineChart(lineChart);
//        dynamicLineChart.testFill();
    }
    @FXML
    void mouseClick() {
        lineChart.setOnMouseClicked((mouseEvent) -> {
            Point2D mouseSceneCoords = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            double x = lineChart.getXAxis().sceneToLocal(mouseSceneCoords).getX();
            double y = lineChart.getYAxis().sceneToLocal(mouseSceneCoords).getY();


            double xValue = lineChart.getXAxis().getValueForDisplay(x).doubleValue();
            double yValue = lineChart.getYAxis().getValueForDisplay(y).doubleValue();

            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            double radiusXPow2 = Math.pow((xAxis.getUpperBound() - xAxis.getLowerBound()) /
                    xAxis.getWidth() * selectionRadius, 2);
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            double radiusYPow2 = Math.pow((yAxis.getUpperBound() - yAxis.getLowerBound()) /
                    yAxis.getHeight() * selectionRadius, 2);
            dynamicLineChart.choose(xValue, yValue, radiusXPow2, radiusYPow2);
            dynamicLineChart.refresh();
        });
    }

    public boolean setInput(InputInformation inputInformation) {
        List<DynamicLineChartData> data;
        try {
            data = DynamicLineChartData.readData(inputInformation);
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
        dynamicLineChart.loadData(data);
        this.inputInformation = inputInformation;
        return true;
    }

    @FXML
    public void changeSelectedRadius() {
        selectionRadius = selectingRadiusSlider.getValue();
    }

    @FXML
    public void addSelected() {
        dynamicLineChart.addSelected();
    }

    @FXML
    public void discardSelected() {
        dynamicLineChart.discardSelected();
    }

    @FXML
    public void unselectAll() {
        dynamicLineChart.unselectAll();
    }
    @FXML
    void saveFile() {
        try {
            try (InputStream is = new FileInputStream(inputInformation.path)) {
                try (Workbook wb = new XSSFWorkbook(is)) {
                    Sheet sheet = wb.getSheetAt(0);
                    List<Integer> usingRows = dynamicLineChart.getUsingRows();
                    usingRows.sort((x, y) -> x - y);
                    for (int i = inputInformation.from, rowsIt = 0; i < inputInformation.to; i++) {
                        String result = ".";
                        if (rowsIt != usingRows.size() && usingRows.get(rowsIt) == i) {
                            rowsIt++;
                            result = "+";
                        }
                        sheet.getRow(i).createCell(inputInformation.result).setCellValue(result);
                    }
                    try (OutputStream os = new FileOutputStream(inputInformation.path)) {
                        wb.write(os);
                    }
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    @FXML
    public void openGetInputWindow() throws IOException {
        Stage stage = new Stage();

        String path = "inputWindow.fxml";

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource(path).openStream());
        InputWindowController controller = fxmlLoader.getController();

        stage.setTitle("Open file");
        stage.setScene(new Scene(root));
        stage.setAlwaysOnTop(false);
        stage.show();
        controller.mainController = this;
        controller.stage = stage;
    }
    @FXML
    public void selectAll() {
        dynamicLineChart.selectAll();
    }
}