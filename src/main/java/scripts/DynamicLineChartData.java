package scripts;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class DynamicLineChartData implements Comparable<DynamicLineChartData>{
    final double x, y;
    final int row;
    SeriesType series;


    DynamicLineChartData(double x, double y, int row, SeriesType series) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.series = series;
    }
    DynamicLineChartData(double x, double y, int row) {
        this(x, y, row, SeriesType.USING);
    }

    @Override
    public int compareTo(DynamicLineChartData data) {
        return Double.compare(this.x, data.x);
    }

    static List<DynamicLineChartData> readData(InputInformation input) throws IOException {
        List<DynamicLineChartData> inputData = new ArrayList<>();
        try(InputStream is = new FileInputStream(input.path)) {
            try(Workbook wb = new XSSFWorkbook(is)) {
                Sheet s = wb.getSheetAt(0);
                for (int i = input.from; i < input.to; i++) {
                    Row row = s.getRow(i);
                    double x = getDoubleFromCell(row.getCell(input.x));
                    double y = getDoubleFromCell(row.getCell(input.y));
                    Cell resultCell = row.getCell(input.result);
                    SeriesType type = SeriesType.DISCARDED;
                    if (resultCell != null && resultCell.getStringCellValue().equals("+")) {
                        type = SeriesType.USING;
                    }
                    inputData.add(new DynamicLineChartData(x, y, i, type));
                }
            }
        }
        return inputData;
    }
    private static double getDoubleFromCell(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return Double.parseDouble(cell.getStringCellValue());
        }
        return cell.getNumericCellValue();
    }
}

enum SeriesType {
    USING, DISCARDED
}