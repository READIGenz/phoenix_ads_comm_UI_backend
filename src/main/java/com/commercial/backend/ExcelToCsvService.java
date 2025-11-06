package com.commercial.backend;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExcelToCsvService {

    /**
     * Convert each sheet of an Excel file into a CSV file,
     * then zip them all into one byte[].
     */
    public byte[] convertExcelToZipOfCsv(InputStream excelInputStream) throws IOException {
        ByteArrayOutputStream zipOutStream = new ByteArrayOutputStream();

        try (Workbook workbook = WorkbookFactory.create(excelInputStream);
             ZipOutputStream zipOut = new ZipOutputStream(zipOutStream)) {

            DataFormatter formatter = new DataFormatter();

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String sheetName = sheet.getSheetName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
                String csvFileName = sheetName + ".csv";

                List<List<String>> rowsData = new ArrayList<>();

                // Step 1: Read all rows into memory
                int maxColumns = 0;
                for (Row row : sheet) {
                    if (row == null) continue;
                    List<String> rowValues = new ArrayList<>();
                    int lastCellNum = row.getLastCellNum() > 0 ? row.getLastCellNum() : 0;

                    for (int cn = 0; cn < lastCellNum; cn++) {
                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        String value = "";
                        if (cell != null) {
                            value = formatter.formatCellValue(cell).trim();
                        }
                        rowValues.add(value);
                    }

                    // track max column count seen
                    maxColumns = Math.max(maxColumns, rowValues.size());
                    rowsData.add(rowValues);
                }

                // Step 2: Find the real last non-empty column across all rows
                int lastNonEmptyCol = -1;
                for (int col = maxColumns - 1; col >= 0; col--) {
                    boolean hasData = false;
                    for (List<String> row : rowsData) {
                        if (col < row.size()) {
                            String val = row.get(col);
                            if (val != null && !val.trim().isEmpty()) {
                                hasData = true;
                                break;
                            }
                        }
                    }
                    if (hasData) {
                        lastNonEmptyCol = col;
                        break;
                    }
                }

                // If entire sheet is empty, skip it
                if (lastNonEmptyCol == -1) continue;

                // Step 3: Normalize all rows to the same number of columns
                for (List<String> row : rowsData) {
                    while (row.size() < lastNonEmptyCol + 1) {
                        row.add(""); // pad missing cells
                    }
                    while (row.size() > lastNonEmptyCol + 1) {
                        row.remove(row.size() - 1); // remove excess blanks
                    }
                }

                // Step 4: Write CSV
                ByteArrayOutputStream csvBytes = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(csvBytes))) {
                    for (List<String> row : rowsData) {
                        for (int i = 0; i < row.size(); i++) {
                            writer.write(escapeForCsv(row.get(i)));
                            if (i < row.size() - 1) {
                                writer.write(",");
                            }
                        }
                        writer.newLine();
                    }
                    writer.flush();
                }

                // Step 5: Add CSV to ZIP
                zipOut.putNextEntry(new ZipEntry(csvFileName));
                zipOut.write(csvBytes.toByteArray());
                zipOut.closeEntry();
            }
        }

        return zipOutStream.toByteArray();
    }

    private String escapeForCsv(String value) {
        if (value == null) return "";
        boolean needQuotes = value.contains(",") || value.contains("\"") ||
                value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }
}