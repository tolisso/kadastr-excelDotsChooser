package scripts;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.util.*;
import java.util.function.Predicate;

class DynamicLineChart {

    private LineChart<Number, Number> lineChart;
    private List<DynamicLineChartData> selectedData;
    private List<DynamicLineChartData> unselectedData;
    private LineChart.Series<Number, Number> usingSeries;
    private LineChart.Series<Number, Number> discardedSeries;
    private LineChart.Series<Number, Number> selectedSeries;

    public DynamicLineChart(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getYAxis().setAutoRanging(false);

        usingSeries = new LineChart.Series<>();
        selectedSeries = new LineChart.Series<>();
        discardedSeries = new LineChart.Series<>();
        usingSeries.setName("Using");
        selectedSeries.setName("Selected");
        discardedSeries.setName("Discarded");
        //populating the series with data

        Platform.runLater(() -> {
            lineChart.getData().add(usingSeries);
            lineChart.getData().add(selectedSeries);
            lineChart.getData().add(discardedSeries);
        });

        selectedData = new ArrayList<>();
        unselectedData = new ArrayList<>();
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    public void choose(double centerX, double centerY, double radiusXPow2, double radiusYPow2) {
        List<Integer> choosed = new ArrayList<>();
        for (int i = 0; i < unselectedData.size(); i++) {
            if (isDataInRadius(unselectedData.get(i), centerX, centerY, radiusXPow2, radiusYPow2)) {
                choosed.add(i);
            }
        }
        putData(selectedData, popChosen(unselectedData, choosed));
    }

    List<Integer> getUsingRows() {
        List<Integer> rows = new ArrayList<>();
        for (var elem : selectedData) {
            if (elem.series == SeriesType.USING) {
                rows.add(elem.row);
            }
        }
        for (var elem : unselectedData) {
            if (elem.series == SeriesType.USING) {
                rows.add(elem.row);
            }
        }
        return rows;
    }

    private boolean isDataInRadius(DynamicLineChartData data, double centerX, double centerY,
                                   double radiusXPow2, double radiusYPow2) {
        return Math.pow(data.x - centerX, 2) / radiusXPow2 + Math.pow(data.y - centerY, 2) / radiusYPow2 <= 1;
    }

    private static DynamicLineChartData[] popChosen(List<DynamicLineChartData> elms, List<Integer> indexes) {
        DynamicLineChartData[] popData = new DynamicLineChartData[indexes.size()];
        int popDataPos = 0;
        int first = 0;
        for (int last = 0, indexPos = 0; last < elms.size(); last++) {
            if (indexPos != indexes.size() && indexes.get(indexPos) == last) {
                indexPos++;
                popData[popDataPos++] = elms.get(last);
            } else {
                DynamicLineChartData tmp = elms.get(first);
                elms.set(first, elms.get(last));
                elms.set(last, tmp);
                first++;
            }
        }
        for (int i = elms.size() - 1; i >= first; i--) {
            elms.remove(i);
        }
        return popData;
    }

    private static void putData(List<DynamicLineChartData> dataContainer, DynamicLineChartData[] elms) {
        int dataContainerPos = dataContainer.size() - 1;
        int elmsPos = elms.length - 1;
        for (int i = 0; i < elms.length; i++) {
            dataContainer.add(null);
        }
        for (int resultPos = dataContainer.size() - 1; resultPos >= 0; resultPos--) {
            if (elmsPos == -1) {
                dataContainer.set(resultPos, dataContainer.get(dataContainerPos--));
            } else if (dataContainerPos == -1) {
                dataContainer.set(resultPos, elms[elmsPos--]);
            } else {
                if (dataContainer.get(dataContainerPos).compareTo(elms[elmsPos]) > 0) {
                    dataContainer.set(resultPos, elms[elmsPos--]);
                } else {
                    dataContainer.set(resultPos, dataContainer.get(dataContainerPos--));
                }
            }
        }
    }
    private static void putData(List<DynamicLineChartData> dataContainer, List<DynamicLineChartData> elms) {
        putData(dataContainer, elms.toArray(new DynamicLineChartData[0]));
    }

        public void refresh() {
        usingSeries.getData().clear();
        selectedSeries.getData().clear();
        discardedSeries.getData().clear();
        usingSeries.getData().addAll(getValuesFromData(unselectedData, (d) -> d.series == SeriesType.USING));
        selectedSeries.getData().addAll(getValuesFromData(selectedData));
        discardedSeries.getData().addAll(getValuesFromData(unselectedData, (d) -> d.series == SeriesType.DISCARDED));
    }

    public void unselectAll() {
        putData(unselectedData, selectedData);
        selectedData.clear();
        refresh();
    }

    public void addSelected() {
        for (var elem : selectedData) {
            elem.series = SeriesType.USING;
        }
        unselectAll();
    }

    public void discardSelected() {
        for (var elem : selectedData) {
            elem.series = SeriesType.DISCARDED;
        }
        unselectAll();
    }

    public void loadData(List<DynamicLineChartData> data) {
        unselectedData.clear();
        selectedData.clear();
        data.sort((a, b) -> a.compareTo(b));
        unselectedData.addAll(data);
        refreshXYAxis(data);
        refresh();
    }

    public void refreshXYAxis(List<DynamicLineChartData> data) {
        double minX = 0, maxX = 10, minY = 0, maxY = 10;
        if (data.size() != 0) {
            minX = data.get(0).x;
            maxX = data.get(0).x;
            minY = data.get(0).y;
            maxY = data.get(0).y;
            for (int i = 1; i < data.size(); i++) {
                minX = Math.min(data.get(i).x, minX);
                maxX = Math.max(data.get(i).x, maxX);
                minY = Math.min(data.get(i).y, minY);
                maxY = Math.max(data.get(i).y, maxY);
            }
        }
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        double borderX = (maxX - minX) / 15;
        double borderY = (maxY - minY) / 10;

        xAxis.setTickUnit((maxX - minX) / 8);
        yAxis.setTickUnit((maxY - minY) / 6);
        xAxis.setLowerBound(minX - borderX);
        xAxis.setUpperBound(maxX + borderX);

        yAxis.setLowerBound(minY - borderY);
        yAxis.setUpperBound(maxY + borderY);
    }

    private static List<LineChart.Data<Number, Number>> getValuesFromData(List<DynamicLineChartData> data,
                                                                          Predicate<DynamicLineChartData> predicate) {
        List<LineChart.Data<Number, Number>> values = new ArrayList<>();
        for (var elem : data) {
            if (predicate.test(elem)) {
                values.add(new LineChart.Data<>(elem.x, elem.y));
            }
        }
        return values;
    }

    private static List<LineChart.Data<Number, Number>> getValuesFromData(List<DynamicLineChartData> data) {
        return getValuesFromData(data, (d) -> true);
    }

    public void selectAll() {
        putData(selectedData, unselectedData);
        unselectedData.clear();
        refresh();
    }
}
