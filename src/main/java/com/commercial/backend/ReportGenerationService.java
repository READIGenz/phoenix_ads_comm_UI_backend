package com.commercial.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@AutoConfiguration
@Service
public class ReportGenerationService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ReportGenerationService() {
    }

    @GetMapping({"/generateReport"})
    @ResponseBody
    public ResponseEntity<byte[]> generateReport() throws IOException {
        String query = this.loadQueryFromProperties();
        if (query != null && !query.isEmpty()) {
            List<Map<String, Object>> resultList = this.jdbcTemplate.queryForList(query);
            if (resultList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.TEXT_PLAIN).body("No data found for the query.".getBytes());
            } else {
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Id,unique_commercial_id,addr_rejects,application,borrower_rejects,credit_facility_rejects,dishonour_rejects,gurantor_rejects,insert_date,relationship_rejects,security_seg_rejects").append("\n");
                Iterator var4 = resultList.iterator();

                while(var4.hasNext()) {
                    Map<String, Object> row = (Map)var4.next();
                    String id = String.valueOf(row.get("Id"));
                    String unique_commercial_id = String.valueOf(row.get("unique_commercial_id"));
                    String addr_rejects = String.valueOf(row.get("addr_rejects"));
                    String application = String.valueOf(row.get("application"));
                    String borrower_rejects = String.valueOf(row.get("borrower_rejects"));
                    String credit_facility_rejects = String.valueOf(row.get("credit_facility_rejects"));
                    String dishonour_rejects = String.valueOf(row.get("dishonour_rejects"));
                    String gurantor_rejects = String.valueOf(row.get("gurantor_rejects"));
                    String insert_date = String.valueOf(row.get("insert_date"));
                    String relationship_rejects = String.valueOf(row.get("relationship_rejects"));
                    String security_seg_rejects = String.valueOf(row.get("security_seg_rejects"));
                    csvContent.append(id).append(",").append(unique_commercial_id).append(",").append(addr_rejects).append(",").append(application).append(",").append(borrower_rejects).append(",").append(credit_facility_rejects).append(",").append(dishonour_rejects).append(",").append(gurantor_rejects).append(",").append(insert_date).append(",").append(relationship_rejects).append(",").append(security_seg_rejects).append("\n");
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(ContentDisposition.attachment().filename("status_table.csv").build());
                return new ResponseEntity(csvContent.toString().getBytes(), headers, HttpStatus.OK);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("Error: Query not found in properties file.".getBytes());
        }
    }

    private String loadQueryFromProperties() throws IOException {
        Properties props = new Properties();

        try {
            InputStream inputStream = (new ClassPathResource("/SQL/queries.properties")).getInputStream();

            try {
                props.load(inputStream);
            } catch (Throwable var6) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException var7) {
            var7.printStackTrace();
            return null;
        }

        return props.getProperty("query.select.status");
    }
}
