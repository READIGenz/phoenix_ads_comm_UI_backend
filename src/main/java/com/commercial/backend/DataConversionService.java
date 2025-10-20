package com.commercial.backend;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@AutoConfiguration
@Service
public class DataConversionService {
    private static final Logger logger = Logger.getLogger(DataConversionService.class.getName());
    @Autowired
    CommercialConstants commercialConstants;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String performDataConversion() {
        StringBuilder responseBuilder = new StringBuilder();

        try {
            // Load the log file path from properties file
            Properties props = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream(commercialConstants.getQueriesPropertiesFile())) {
                props.load(inputStream);
            }
            String filepath = props.getProperty(commercialConstants.getLogFilePath());

            // Delete the existing log file if it exists
            try {
                Path path = Paths.get(filepath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Handle any errors related to deleting the existing log file
                e.printStackTrace();
            }

            // Configure the root logger
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setUseParentHandlers(false); // Disable default console handler

            try {
                // Create and configure the FileHandler
                FileHandler fileHandler = new FileHandler(filepath, true); // Append mode
                fileHandler.setFormatter(new SimpleFormatter()); // Optional formatter
                rootLogger.addHandler(fileHandler);
            } catch (IOException e) {
                // Handle any errors related to setting up the FileHandler
                e.printStackTrace();
            }

            // Establish connection
            Connection connection = jdbcTemplate.getDataSource().getConnection();

            // Drop procedures if they exist
            dropProcedureIfExists(connection, commercialConstants.getTruncateAndDropProc());
            dropProcedureIfExists(connection, commercialConstants.getMoveDataProc());

            // Execute truncate procedure
            executeStoredProcedure(connection, commercialConstants.getTruncateSqlPath(), commercialConstants.getTruncateAndDropProc());

            // Execute data migration procedure
            executeStoredProcedure(connection, commercialConstants.getMigrateSqlPath(), commercialConstants.getMoveDataProc());

            // Fetch record counts for the tables
            int borrowerSegCount = getTableCount(connection, commercialConstants.getBorrowerSegTable());
            int addressCount = getTableCount(connection, commercialConstants.getAddressSegCommercialTable());
            int creditFacilitySegCount = getTableCount(connection, commercialConstants.getCreditFacilitySegTable());
            int dishonourCount = getTableCount(connection, commercialConstants.getDishonourOfChequeSegTable());
            int gurantorSegCount = getTableCount(connection, commercialConstants.getGuarantorSegTable());
            int relationship_segCount = getTableCount(connection, commercialConstants.getRelationshipSegTable());
            int securitySegCount = getTableCount(connection, commercialConstants.getSecuritySegTable());


            // Build the response
            responseBuilder.append(commercialConstants.getBorrowerSegmentLabel()).append(borrowerSegCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getAddressSegmentLabel()).append(addressCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getCreditFacilitySegmentLabel()).append(creditFacilitySegCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getDishonourSegmentLabel()).append(dishonourCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getGuarantorSegmentLabel()).append(gurantorSegCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getRelSegmentLabel()).append(relationship_segCount).append(commercialConstants.getLineBreak());
            responseBuilder.append(commercialConstants.getSecuritySegmentLabel()).append(securitySegCount).append(commercialConstants.getLineBreak());

            // Close the connection
            connection.close();

            // Logging
            Logger.getLogger(DataConversionService.class.getName()).info("Stored procedures executed successfully.");

        } catch (Exception e) {
            // Exception handling
            e.printStackTrace();
            responseBuilder.append("Error: ").append(e.getMessage()).append(commercialConstants.getLineBreak());

            // Logging
            Logger.getLogger(DataConversionService.class.getName()).severe("Error executing the stored procedures: " + e.getMessage());
        }

        return responseBuilder.toString();
    }

    private void dropProcedureIfExists(Connection connection, String procedureName) throws SQLException {
        String dropProcedureSQL = commercialConstants.getDropQuery() + procedureName;
        try (Statement statement = connection.createStatement()) {
            statement.execute(dropProcedureSQL);
        }
    }

    private void executeStoredProcedure(Connection connection, String sqlFileName, String procedureName) throws SQLException, IOException {
        // Load SQL script
        try (InputStream inputStream = getClass().getResourceAsStream(sqlFileName)) {
            if (inputStream == null) {
                throw new IOException("SQL file not found: " + sqlFileName);
            }
            String sqlScript = IOUtils.toString(inputStream, commercialConstants.getUtf8());

            // Create statement and execute script
            try (Statement statement = connection.createStatement()) {
                statement.execute(sqlScript);
            }

            // Create CallableStatement for stored procedure
            try (CallableStatement callableStatement = connection.prepareCall("{call " + procedureName + "()}")) {
                // Execute stored procedure
                callableStatement.execute();
            }
        }
    }

    private int getTableCount(Connection connection, String tableName) throws SQLException {
        String countQuery = commercialConstants.getCountquery() + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(countQuery)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
    public void executeProcedure(String procedureName, String sqlFilePath) throws IOException, SQLException {
        // Obtain a connection from the jdbcTemplate
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             InputStream inputStream = getClass().getResourceAsStream(sqlFilePath)) {

            // Read the SQL script from the input stream
            String sqlScript = IOUtils.toString(inputStream, commercialConstants.getUtf8());

            // Create statement and execute script
            try (Statement statement = connection.createStatement()) {
                // Drop the procedure if it exists
                String dropProcedure = String.format(commercialConstants.getDropProcedureTemplate(), procedureName);
                statement.executeUpdate(dropProcedure);
                statement.execute(sqlScript);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void runDuplicateProcedure(String date) throws SQLException {
        // Use try-with-resources to automatically close the connection
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             Statement statement = connection.createStatement()) {

            // Create CallableStatement for stored procedure with one parameter
            try (CallableStatement callableStatement = connection.prepareCall(commercialConstants.getDuplicateTablesProcedureCall())) {
                // Set the parameter (assuming it's a string)
                callableStatement.setString(1, date);

                // Execute the stored procedure
                callableStatement.execute();
            } catch (SQLException e) {
                // Handle any SQL exceptions
                e.printStackTrace();
            }
        }
    }


    public void runProcedure(String procedureName) throws SQLException {
        // Format the stored procedure call
        String callProcedure = String.format(commercialConstants.getCallProcedureTemplate(), procedureName);

        try (Connection connection = this.jdbcTemplate.getDataSource().getConnection();
             CallableStatement callableStatement = connection.prepareCall(callProcedure)) {

            // Execute the stored procedure
            callableStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;  // Rethrow to allow handling upstream
        }
    }

    public int countRows(String procedureName, String tableName) throws SQLException {
        String sql = String.format(commercialConstants.getCallParameterTableFormat(), procedureName);

        // Log the stored procedure call attempt
        logger.info("Executing stored procedure: " + procedureName + " for table: " + tableName);

        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // Set input parameter (tableName)
            callableStatement.setString(1, tableName);

            // Execute the stored procedure and retrieve the result set
            try (ResultSet resultSet = callableStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Get the row count and log it
                    int rowCount = resultSet.getInt(1);
                    logger.info("Row count for table " + tableName + ": " + rowCount);
                    return rowCount;
                }
            }
        } catch (SQLException e) {
            // Log the error and rethrow the exception
            logger.severe("Error while counting rows for table " + tableName + ": " + e.getMessage());
            throw e;
        }

        // Log if the result set was empty
        logger.warning("No rows returned for table: " + tableName);
        return 0;
    }
}
