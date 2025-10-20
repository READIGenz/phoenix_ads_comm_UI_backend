package com.commercial.backend;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Service;

import java.io.*;
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

                // Write CSV data into memory
                ByteArrayOutputStream csvBytes = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(csvBytes))) {
                    for (Row row : sheet) {
                        int lastColumn = row.getLastCellNum() > 0 ? row.getLastCellNum() : 0;
                        for (int cn = 0; cn < lastColumn; cn++) {
                            Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            String value = "";
                            if (cell != null) {
                                value = formatter.formatCellValue(cell);
                            }
                            writer.write(escapeForCsv(value));
                            if (cn < lastColumn - 1) {
                                writer.write(",");
                            }
                        }
                        writer.newLine();
                    }
                    writer.flush();
                }

                // Add CSV as new ZIP entry
                zipOut.putNextEntry(new ZipEntry(csvFileName));
                zipOut.write(csvBytes.toByteArray());
                zipOut.closeEntry();
            }

        }

        return zipOutStream.toByteArray();
    }

    private String escapeForCsv(String value) {
        if (value == null) return "";
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }
}