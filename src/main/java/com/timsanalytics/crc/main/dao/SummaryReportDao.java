package com.timsanalytics.crc.main.dao;

import com.timsanalytics.crc.main.beans.SummaryReportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryReportDao {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final JdbcTemplate mySqlAuthJdbcTemplate;

    @Autowired
    public SummaryReportDao(JdbcTemplate mySqlAuthJdbcTemplate) {
        this.mySqlAuthJdbcTemplate = mySqlAuthJdbcTemplate;
    }

    private String getActiveStudents_RootQuery() {
        //noinspection StringBufferReplaceableByString
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      Person_Student.student_id,\n");
        query.append("      Person_Student.surname,\n");
        query.append("      Person_Student.given_name,\n");
        query.append("      Rel_Student_Program_Status.start_date\n");
        query.append("  FROM\n");
        query.append("      CRC.Person_Student\n");
        query.append("      LEFT JOIN CRC.Rel_Student_Program_Status ON Rel_Student_Program_Status.student_id = Person_Student.student_id AND Rel_Student_Program_Status.deleted = 0\n");
        query.append("      LEFT OUTER JOIN CRC.Rel_Student_Program_Status effectiveDateComparison ON\n");
        query.append("      (\n");
        query.append("          effectiveDateComparison.student_id = Person_Student.student_id\n");
        query.append("          AND\n");
        query.append("          (\n");
        query.append("              Rel_Student_Program_Status.start_date < effectiveDateComparison.start_date\n");
        query.append("              OR\n");
        query.append("              (\n");
        query.append("                  Rel_Student_Program_Status.start_date = effectiveDateComparison.start_date\n");
        query.append("                  AND\n");
        query.append("                  Rel_Student_Program_Status.student_program_status_id > effectiveDateComparison.student_program_status_id\n");
        query.append("              )\n");
        query.append("          )\n");
        query.append("      )\n");
        query.append("  WHERE\n");
        query.append("  (\n");
        query.append("      Person_Student.deleted = 0\n");
        query.append("      AND effectiveDateComparison.student_id IS NULL\n");
        query.append("      AND\n");
        query.append("      (\n");
        query.append("          Rel_Student_Program_Status.program_status_level_one_id =1\n");
        query.append("          OR\n");
        query.append("          Rel_Student_Program_Status.program_status_level_one_id IS NULL\n");
        query.append("      )\n");
        query.append("  )\n");
        return query.toString();
    }

    public Integer getActiveStudents_Count() {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      COUNT(*)\n");
        query.append("  FROM\n");
        query.append("  -- ROOT QUERY\n");
        query.append("  (\n");
        query.append(getActiveStudents_RootQuery());
        query.append("  ) AS ROOT_QUERY\n");
        query.append("  -- END ROOT QUERY\n");
        try {
            Integer count = this.mySqlAuthJdbcTemplate.queryForObject(query.toString(), new Object[]{}, Integer.class);
            return count == null ? 0 : count;
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return 0;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return 0;
        }
    }

    public List<SummaryReportResult> getActiveStudents_Results() {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      student_id AS id,\n");
        query.append("      CONCAT(given_name, ' ', surname) AS text\n");
        query.append("  FROM\n");
        query.append("  -- ROOT QUERY\n");
        query.append("  (\n");
        query.append(getActiveStudents_RootQuery());
        query.append("  ) AS ROOT_QUERY\n");
        query.append("  -- END ROOT QUERY\n");
        query.append("  ORDER BY\n");
        query.append("      surname,\n");
        query.append("      given_name\n");
        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{}, (rs, rowNum) -> {
                SummaryReportResult row = new SummaryReportResult();
                row.setId(rs.getInt("id"));
                row.setText(rs.getString("text"));
                return row;
            });
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }
}

