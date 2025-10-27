package com.commercial.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@Service
public class UploadExcelService {

    private static final String SPECIAL_CHARACTERS_REGEX = "[/\\-\\(\\)\\s.#]";
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String REPLACEMENT_CHAR = "_";
    private static final String CLOSE_PARENTHESIS = ")";
    private static final String BORROWER_ID_DEFINITION = "borrower_id BIGINT PRIMARY KEY AUTO_INCREMENT,";
    private static final String CRED_ID_DEFINITION = "cred_id BIGINT PRIMARY KEY AUTO_INCREMENT,";

    private static final String COLUMN_DEFINITION = " VARCHAR(80),";
    private static final String UTF_8 = "UTF-8";
    private static final int BATCH_SIZE = 1000;
    private static final String LINE_BREAK_REGEX = "\\r?\\n";
    private static final String MULTIPLE_SPACES_REGEX = "\\s{2,}";
    private static final String SINGLE_SPACE = " ";
    private static final String SUCCESS_MSG_PART1 = "Success!!! ";
    private static final String SUCCESS_MSG_PART2 = " records were securely stored in the database.";

    @Autowired
    private DataSource dataSource;

    public String handleFileUpload(@RequestParam("files") MultipartFile[] files, Model model) {
        if (files == null || files.length == 0) {
            model.addAttribute("message", "Please select at least one CSV file to upload.");
            return "csv_to_db";
        }

        int totalRecords = 0;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String[] csvColumns;

                // Generate table name from CSV file name
                String originalFileName = file.getOriginalFilename();
                if (originalFileName == null || originalFileName.isEmpty()) {
                    originalFileName = "table_" + System.currentTimeMillis();
                }
                String tableName = cleanColumnName(originalFileName.replace(".csv", ""),
                        SPECIAL_CHARACTERS_REGEX, REPLACEMENT_CHAR, WHITESPACE_REGEX);

                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), UTF_8))) {

                    String currentLine;

                    // Read header line
                    if ((currentLine = bufferedReader.readLine()) != null) {
                        csvColumns = splitCSVLine(currentLine);

                        // Create table dynamically
                        createTable(connection, tableName, csvColumns);

                        String insertQuery = buildInsertQuery(tableName, csvColumns);

                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                            int lineCount = 0;

                            while ((currentLine = bufferedReader.readLine()) != null) {
                                String[] fields = splitCSVLine(currentLine);

                                for (int columnIndex = 0; columnIndex < Math.min(fields.length, csvColumns.length); columnIndex++) {
                                    String sanitizedValue = sanitizeColumnValue(fields[columnIndex]);
                                    preparedStatement.setString(columnIndex + 1, sanitizedValue);
                                }

                                preparedStatement.addBatch();

                                if(tableName.equals("BS")){
                                    lineCount++;
                                }


                                if (lineCount % BATCH_SIZE == 0) {
                                    preparedStatement.executeBatch();
                                }
                            }

                            preparedStatement.executeBatch();
                            totalRecords += lineCount;
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);

            String successMessage = SUCCESS_MSG_PART1 + totalRecords + SUCCESS_MSG_PART2;
            model.addAttribute("successMessage", successMessage);
            return successMessage;

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String[] splitCSVLine(String csvLine) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        char[] characters = csvLine.toCharArray();

        for (char character : characters) {
            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (character == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    private static String cleanColumnName(String columnName, String specialCharactersRegex,
                                          String replacementChar, String whitespaceRegex) {
        return columnName.replaceAll(specialCharactersRegex, replacementChar)
                .replaceAll(whitespaceRegex, replacementChar);
    }

    private void createTable(Connection connection, String tableName, String[] recordsColumns) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);

            StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + tableName + " (");

            if(tableName.equals("BS")){
                createTableQuery.append(BORROWER_ID_DEFINITION);
            }

            for (String column : recordsColumns) {
                createTableQuery.append(cleanColumnName(column, SPECIAL_CHARACTERS_REGEX,
                        REPLACEMENT_CHAR, WHITESPACE_REGEX)).append(COLUMN_DEFINITION);
            }

            createTableQuery.setLength(createTableQuery.length() - 1); // Remove last comma
            createTableQuery.append(CLOSE_PARENTHESIS);
            statement.executeUpdate(createTableQuery.toString());
        }
    }

    private String buildInsertQuery(String tableName, String[] recordsColumns) {
        StringBuilder insertQuery = new StringBuilder("INSERT INTO ");
        insertQuery.append(tableName).append(" (");

        for (String column : recordsColumns) {
            insertQuery.append(cleanColumnName(column, SPECIAL_CHARACTERS_REGEX,
                    REPLACEMENT_CHAR, WHITESPACE_REGEX)).append(",");
        }
        insertQuery.setLength(insertQuery.length() - 1); // Remove last comma
        insertQuery.append(") VALUES (");

        for (int i = 0; i < recordsColumns.length; i++) {
            insertQuery.append("?,");
        }
        insertQuery.setLength(insertQuery.length() - 1);
        insertQuery.append(")");

        return insertQuery.toString();
    }

    private String sanitizeColumnValue(String value) {
        if (value == null) return null;
        return value.trim().replaceAll(LINE_BREAK_REGEX, "")
                .replaceAll(MULTIPLE_SPACES_REGEX, SINGLE_SPACE);
    }
}