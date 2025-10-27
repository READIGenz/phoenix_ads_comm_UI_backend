package com.commercial.backend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource(value = {"classpath:commercialconstants.properties"}, ignoreResourceNotFound = true),
        @PropertySource(value = {"file:config/commercialconstants.properties"}, ignoreResourceNotFound = true)
})
@Getter
@Setter
@NoArgsConstructor
public class CommercialConstants {
    // Success and Error Messages
    @Value("${success.message}")
    private String successMessage;

    @Value("${error.loading.configuration}")
    private String errorLoadingConfiguration;

    @Value("${error.loading.headseg.properties}")
    private String errorLoadingHeadsegProperties;

    @Value("${error.running.jar}")
    private String errorRunningJar;

    @Value("${error.executing.jar}")
    private String errorExecutingJar;

    // Header and File Names
    @Value("${header.seg.cycle.date.reported}")
    private String headerSegCycleDateReported;

    @Value("${application.properties.filename}")
    private String applicationProperties;

    @Value("${headSeg.properties.filename}")
    private String headProperties;

    @Value("${java.command}")
    private String javaCommand;

    @Value("${jar.option}")
    private String jarOption;

    // Datasource Properties
    @Value("${spring.datasource.url.property}")
    private String springDatasourceUrlProperty;

    @Value("${spring.datasource.username.property}")
    private String springDatasourceUsernameProperty;

    @Value("${spring.datasource.password.property}")
    private String springDatasourcePasswordProperty;

    @Value("${spring.jpa.hibernate.ddl-auto.property}")
    private String springJpaHibernateDdlAutoProperty;

    // Procedure Constants
    @Value("${truncate.and.drop.proc}")
    private String truncateAndDropProc;

    @Value("${drop.duplicate.procedure.sql}")
    private String dropDuplicateProcedureSql;

    @Value("${move.data.proc}")
    private String moveDataProc;

    @Value("${truncate.sql.path}")
    private String truncateSqlPath;

    @Value("${migrate.sql.path}")
    private String migrateSqlPath;

    @Value("${log.file.path}")
    private String logFilePath;

    @Value("${queries.properties.file}")
    private String queriesPropertiesFile;

    @Value("${count.query}")
    private String countquery;

    @Value("${duplicate.table.sql.path}")
    private String duplicateTableSqlPath;

    @Value("${drop.query}")
    private String dropQuery;

    @Value("${duplicate.tables.procedure.call}")
    private String duplicateTablesProcedureCall;

    // Table Names
    @Value("${borrower.seg.table}")
    private String borrowerSegTable;

    @Value("${address.seg.commercial.table}")
    private String addressSegCommercialTable;

    @Value("${credit.facility.seg.table}")
    private String creditFacilitySegTable;

    @Value("${dishonour.of.cheque.seg.table}")
    private String dishonourOfChequeSegTable;

    @Value("${guarantor.seg.table}")
    private String guarantorSegTable;

    @Value("${relationship.seg.table}")
    private String relationshipSegTable;

    @Value("${security.seg.table}")
    private String securitySegTable;

    // Encoding
    @Value("${encoding.utf8}")
    private String utf8;

    // Environment Variable Constants
    @Value("${spring.datasource.env.url}")
    private String springDatasourceEnvUrl;

    @Value("${spring.datasource.env.username}")
    private String springDatasourceEnvUsername;

    @Value("${spring.datasource.env.password}")
    private String springDatasourceEnvPassword;

    @Value("${spring.jpa.hibernate.ddl-auto.env}")
    private String springJpaHibernateDdlAutoEnv;

    @Value("${app.drop-backup-procedure}")
    private String dropBackupProcedure;

    @Value("${app.drop-backup-path}")
    private String dropBackupPath;

    @Value("${drop.procedure.template}")
    private String dropProcedureTemplate;

    @Value("${app.duplicate-tables-procedure}")
    private String duplicateTablesProcedure;

    @Value("${call.procedure.template}")
    private String callProcedureTemplate;

    @Value("${app.count-rows-table}")
    private String countRowsTable;

    @Value("${app.count-rows-table-path}")
    private String countRowsTablePath;

    @Value("${app.status-table}")
    private String statusTable;

    @Value("${call.procedure.procedure.template}")
    private String callParameterTableFormat;

    @Value("${sql.truncate.table}")
    private String truncateTableFormat;

    // Inject segment labels from the properties file
    @Value("${segment.borrower}")
    private String borrowerSegmentLabel;

    @Value("${segment.creditfacility}")
    private String creditFacilitySegmentLabel;

    @Value("${segment.address}")
    private String addressSegmentLabel;

    @Value("${segment.dishonour}")
    private String dishonourSegmentLabel;

    @Value("${segment.guarantor}")
    private String guarantorSegmentLabel;

    @Value("${segment.relationship}")
    private String relSegmentLabel;

    @Value("${segment.security}")
    private String securitySegmentLabel;

    @Value("${line.break}")
    private String lineBreak;

    @Value("${app.truncate.create.table.path}")
    private String truncateCreateTablePath;

    @Value("${app.truncate.create.table.procedure}")
    private String truncateCreateTableProcedure;

    @Value("${app.duplicate-table-path}")
    private String duplicateTablePath;

    // ========== Borrower Segment ==========
    @Value("${migrate.sql.borrower}")
    private String borrowerSqlPath;
    @Value("${procedure.name.borrower}")
    private String borrowerProc;

    // ========== Address Segment ==========
    @Value("${migrate.sql.address}")
    private String addressSqlPath;
    @Value("${procedure.name.address}")
    private String addressProc;

    // ========== Credit Facility Segment ==========
    @Value("${migrate.sql.creditfacility}")
    private String creditFacilitySqlPath;
    @Value("${procedure.name.creditfacility}")
    private String creditFacilityProc;

    // ========== Dishonour of Cheque Segment ==========
    @Value("${migrate.sql.dishonour}")
    private String dishonourSqlPath;
    @Value("${procedure.name.dishonour}")
    private String dishonourProc;

    // ========== Guarantor Segment ==========
    @Value("${migrate.sql.guarantor}")
    private String guarantorSqlPath;
    @Value("${procedure.name.guarantor}")
    private String guarantorProc;

    // ========== Relationship Segment ==========
    @Value("${migrate.sql.relationship}")
    private String relationshipSqlPath;
    @Value("${procedure.name.relationship}")
    private String relationshipProc;

    // ========== Security Segment ==========
    @Value("${migrate.sql.security}")
    private String securitySqlPath;
    @Value("${procedure.name.security}")
    private String securityProc;
}

