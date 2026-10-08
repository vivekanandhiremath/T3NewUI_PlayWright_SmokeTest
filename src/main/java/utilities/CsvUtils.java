package utilities;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtils {

    private CustomLogger logger;

    public CsvUtils(CustomLogger logger) {
        this.logger = logger;
    }

    /**
     * Read data from CSV file
     */
    public List<String[]> readCsv(String filePath) throws IOException, CsvException {
        logger.logInfo("Reading CSV from: " + filePath);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> data = reader.readAll();
            logger.logInfo("Loaded " + data.size() + " rows from CSV");
            return data;
        }
    }

    /**
     * Write data to CSV file
     */
    public void writeCsv(String filePath, List<String[]> data) throws IOException {
        logger.logInfo("Writing CSV to: " + filePath);
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath), ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            writer.writeAll(data);
            logger.logInfo("CSV written successfully");
        }
    }

    /**
     * Update a specific cell in CSV data (in memory)
     */
    public void updateCell(List<String[]> data, int rowIndex, int columnIndex, String value) {
        if (rowIndex >= data.size()) {
            logger.logWarning("Row index " + rowIndex + " out of bounds");
            return;
        }
        String[] row = data.get(rowIndex);
        if (columnIndex >= row.length) {
            String[] newRow = new String[columnIndex + 1];
            System.arraycopy(row, 0, newRow, 0, row.length);
            row = newRow;
        }
        row[columnIndex] = value;
        data.set(rowIndex, row);
        logger.logInfo("Updated row " + rowIndex + ", column " + columnIndex + " with value: " + value);
    }

    /**
     * Get value from CSV data
     */
    public String getCell(List<String[]> data, int rowIndex, int columnIndex) {
        if (rowIndex >= data.size()) return null;
        String[] row = data.get(rowIndex);
        if (columnIndex >= row.length) return null;
        return row[columnIndex];
    }
}

