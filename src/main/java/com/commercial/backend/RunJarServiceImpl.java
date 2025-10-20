package com.commercial.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

@AutoConfiguration
@Service
@RequestMapping("/api")
public class RunJarServiceImpl  {

    @Value("${jar.file.path}")
    private String jarFilePath;

    @Value("${config.folder.path}")
    private String configFolderPath;

    @Autowired
    DataConversionService dataConversionService;
    @Autowired
    CommercialConstants commercialConstants;

    @GetMapping("/run-jar")
    public String runJar() throws SQLException, IOException {
        dataConversionService.executeProcedure(commercialConstants.getTruncateCreateTableProcedure(),commercialConstants.getTruncateCreateTablePath());
        dataConversionService.executeProcedure(commercialConstants.getCountRowsTable(),commercialConstants.getCountRowsTablePath());
        try {
            // Load configuration properties from application.properties
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream(new File(configFolderPath, commercialConstants.getApplicationProperties()))) {
                properties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error loading configuration: " + e.getMessage();
            }

            // Load additional properties from headSeg.properties
            Properties headSegProperties = new Properties();
            try (InputStream input = new FileInputStream(new File(configFolderPath, commercialConstants.getHeadProperties()))) {
                headSegProperties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error loading headSeg.properties: " + e.getMessage();
            }
            dataConversionService.runProcedure(commercialConstants.getTruncateCreateTableProcedure());

            // Build the command to run the JAR file
            ProcessBuilder processBuilder = new ProcessBuilder(commercialConstants.getJavaCommand(), commercialConstants.getJarOption(), jarFilePath);
            processBuilder.environment().put(commercialConstants.getSpringDatasourceEnvUrl(), properties.getProperty(commercialConstants.getSpringDatasourceUrlProperty()));
            processBuilder.environment().put(commercialConstants.getSpringDatasourceEnvUsername(), properties.getProperty(commercialConstants.getSpringDatasourceUsernameProperty()));
            processBuilder.environment().put(commercialConstants.getSpringDatasourceEnvPassword(), properties.getProperty(commercialConstants.getSpringDatasourcePasswordProperty()));
            processBuilder.environment().put(commercialConstants.getSpringJpaHibernateDdlAutoEnv(), properties.getProperty(commercialConstants.getSpringJpaHibernateDdlAutoProperty()));

            // Add additional properties from headSeg.properties to the environment
            for (String propertyName : headSegProperties.stringPropertyNames()) {
                processBuilder.environment().put(propertyName, headSegProperties.getProperty(propertyName));
            }

            // Start the process to run the JAR file
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            int rowCount = dataConversionService.countRows(commercialConstants.getCountRowsTable(), commercialConstants.getStatusTable());

            if (exitCode == 0) {
                String message = "Success! Your file for submission to the CICs has been generated."
                        + "The file contains " + rowCount + " records.";
                return message;
            } else {
                String message = "Error running the JAR file.";
                return message;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error executing the JAR file: " + e.getMessage();
        }
    }
}