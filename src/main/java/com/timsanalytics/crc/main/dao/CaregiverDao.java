package com.timsanalytics.crc.main.dao;

import com.timsanalytics.crc.common.beans.KeyValue;
import com.timsanalytics.crc.common.beans.ServerSidePaginationRequest;
import com.timsanalytics.crc.main.beans.Caregiver;
import com.timsanalytics.crc.main.dao.RowMappers.CaregiverRowMapper;
import com.timsanalytics.crc.utils.GenerateUuidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;

@Service
public class CaregiverDao {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final JdbcTemplate mySqlAuthJdbcTemplate;
    private final GenerateUuidService generateUuidService;

    @Autowired
    public CaregiverDao(JdbcTemplate mySqlAuthJdbcTemplate, GenerateUuidService generateUuidService) {
        this.mySqlAuthJdbcTemplate = mySqlAuthJdbcTemplate;
        this.generateUuidService = generateUuidService;
    }

    public Caregiver createCaregiver(Caregiver caregiver) {
        StringBuilder query = new StringBuilder();
        query.append("  INSERT INTO\n");
        query.append("      HCW_DATA.PERSON\n");
        query.append("      (\n");
        query.append("          PERSON.PERSON_GUID,\n");
        query.append("          PERSON.PERSON_SURNAME,\n");
        query.append("          PERSON.PERSON_GIVEN_NAME,\n");
        query.append("          PERSON.PERSON_GENDER,\n");
        query.append("          PERSON.STATUS\n");
        query.append("      )\n");
        query.append("      VALUES\n");
        query.append("      (\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          'Active'\n");
        query.append("      )\n");
        this.logger.trace("SQL:\n" + query.toString());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        caregiver.setCaregiverGuid(this.generateUuidService.GenerateUuid());
                        this.logger.trace("New Caregiver GUID: " + caregiver.getCaregiverGuid());
                        ps.setString(1, caregiver.getCaregiverGuid());
                        ps.setString(2, caregiver.getCaregiverSurname());
                        ps.setString(3, caregiver.getCaregiverGivenName());
                        ps.setString(4, caregiver.getCaregiverGender());
                        return ps;
                    }
            );
            return this.getCaregiverDetail(caregiver.getCaregiverGuid());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }
    
    public int getCaregiverList_SSP_TotalRecords(ServerSidePaginationRequest<Caregiver> serverSidePaginationRequest) {
        StringBuilder query = new StringBuilder();
        query.append("          SELECT\n");
        query.append("              COUNT(*)\n");
        query.append("          FROM\n");
        query.append("          -- ROOT QUERY\n");
        query.append("          (\n");
        query.append(getCaregiverList_SSP_RootQuery(serverSidePaginationRequest));
        query.append("          ) AS ROOT_QUERY\n");
        query.append("          -- END ROOT QUERY\n");
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

    public List<Caregiver> getCaregiverList_SSP(ServerSidePaginationRequest<Caregiver> serverSidePaginationRequest) {
        int pageStart = (serverSidePaginationRequest.getPageIndex()) * serverSidePaginationRequest.getPageSize();
        int pageSize = serverSidePaginationRequest.getPageSize();

        String sortColumn = serverSidePaginationRequest.getSortColumn();
        String sortDirection = serverSidePaginationRequest.getSortDirection();

        StringBuilder query = new StringBuilder();
        query.append("  -- PAGINATION QUERY\n");
        query.append("  SELECT\n");
        query.append("      FILTER_SORT_QUERY.*\n");
        query.append("  FROM\n");

        query.append("      -- FILTER/SORT QUERY\n");
        query.append("      (\n");
        query.append("          SELECT\n");
        query.append("              *\n");
        query.append("          FROM\n");

        query.append("          -- ROOT QUERY\n");
        query.append("          (\n");
        query.append(getCaregiverList_SSP_RootQuery(serverSidePaginationRequest));
        query.append("          ) AS ROOT_QUERY\n");
        query.append("          -- END ROOT QUERY\n");

        query.append("          ORDER BY\n");
        query.append(sortColumn).append(" ").append(sortDirection.toUpperCase()).append(",\n");
        query.append("              last_name,\n");
        query.append("              first_name\n");
        query.append("      ) AS FILTER_SORT_QUERY\n");
        query.append("      -- END FILTER/SORT QUERY\n");

        query.append("  LIMIT ?, ?\n");
        query.append("  -- END PAGINATION QUERY\n");

        this.logger.trace("SQL:\n" + query.toString());
        this.logger.trace("pageStart=" + pageStart + ", pageSize=" + pageSize);

        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{
                    pageStart,
                    pageSize
            }, (rs, rowNum) -> {
                Caregiver row = new Caregiver();
                row.setCaregiverGuid(rs.getString("id"));
                row.setCaregiverSurname(rs.getString("last_name"));
                row.setCaregiverGivenName(rs.getString("first_name"));
                return row;
            });
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            this.logger.error("pageStart=" + pageSize + ", pageSize=" + pageSize);
            return null;
        }
    }

    private String getCaregiverList_SSP_RootQuery(ServerSidePaginationRequest<Caregiver> serverSidePaginationRequest) {
        //noinspection StringBufferReplaceableByString
        StringBuilder query = new StringBuilder();
        query.append("              SELECT\n");
        query.append("                  Person.id,\n");
        query.append("                  Person.first_name,\n");
        query.append("                  Person.last_name\n");
        query.append("              FROM\n");
        query.append("                  CRC.Person\n");
        query.append("              WHERE\n");
        query.append("              (\n");
        query.append("                      Person.Deleted = 0\n");
        query.append("                      AND Person.person_type_id = 3\n");
        query.append("                  AND\n");
        query.append(getCaregiverList_SSP_AdditionalWhereClause(serverSidePaginationRequest));
        query.append("              )");
        return query.toString();
    }

    private String getCaregiverList_SSP_AdditionalWhereClause(ServerSidePaginationRequest<Caregiver> serverSidePaginationRequest) {
        StringBuilder whereClause = new StringBuilder();
        String nameFilter = serverSidePaginationRequest.getNameFilter() != null ? serverSidePaginationRequest.getNameFilter() : "";

        // NAME FILTER CLAUSE
        if (!"".equalsIgnoreCase(nameFilter)) {
            whereClause.append("                  (\n");
            whereClause.append("                    UPPER(PERSON.PERSON_SURNAME) LIKE UPPER('%").append(nameFilter).append("%')\n");
            whereClause.append("                    OR");
            whereClause.append("                    UPPER(PERSON.PERSON_GIVEN_NAME) LIKE UPPER('%").append(nameFilter).append("%')\n");
            whereClause.append("                  )\n");
        } else {
            whereClause.append("                  (1=1)");
        }

        return whereClause.toString();
    }

    public Caregiver getCaregiverDetail(String caregiverGuid) {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      id,\n");
        query.append("      last_name,\n");
        query.append("      first_name\n");
        query.append("  FROM\n");
        query.append("      CRC.Person\n");
        query.append("  WHERE\n");
        query.append("      id = ?\n");
        this.logger.trace("SQL:\n" + query.toString());
        try {
            return this.mySqlAuthJdbcTemplate.queryForObject(query.toString(), new Object[]{caregiverGuid}, new CaregiverRowMapper());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public Caregiver updateCaregiver(Caregiver caregiver) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Person\n");
        query.append("  SET\n");
        query.append("      last_name = ?,\n");
        query.append("      first_name = ?\n");
        query.append("  WHERE\n");
        query.append("      id = ?\n");
        this.logger.trace("SQL:\n" + query.toString());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setString(1, caregiver.getCaregiverSurname());
                        ps.setString(2, caregiver.getCaregiverGivenName());
                        ps.setString(3, caregiver.getCaregiverGuid());
                        return ps;
                    }
            );
            return this.getCaregiverDetail(caregiver.getCaregiverGuid());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public KeyValue deleteCaregiver(String caregiverGuid) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      HCW_DATA.PERSON\n");
        query.append("  SET\n");
        query.append("      STATUS = 'Deleted'\n");
        query.append("  WHERE\n");
        query.append("      PERSON_GUID = ?\n");
        this.logger.trace("SQL:\n" + query.toString());
        this.logger.trace("PERSON_GUID=" + caregiverGuid);
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setString(1, caregiverGuid);
                        return ps;
                    }
            );
            return new KeyValue("caregiverGuid", caregiverGuid);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public List<Caregiver> getCaregiverListBystudentId(String studentId) {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      RELATIONSHIP_GUID,\n");
        query.append("      STUDENT_GUID,\n");
        query.append("      RELATIONSHIP.PERSON_GUID,\n");
        query.append("      PERSON.PERSON_SURNAME,\n");
        query.append("      PERSON.PERSON_GIVEN_NAME,\n");
        query.append("      RELATIONSHIP.RELATIONSHIP_TYPE,\n");
        query.append("      RELATIONSHIP.RELATIONSHIP_BLOOD_RELATIVE\n");
        query.append("  FROM\n");
        query.append("      HCW_DATA.RELATIONSHIP\n");
        query.append("      LEFT JOIN HCW_DATA.PERSON ON PERSON.PERSON_GUID = RELATIONSHIP.PERSON_GUID\n");
        query.append("  WHERE\n");
        query.append("      STUDENT_GUID = ?\n");
        this.logger.trace("SQL:\n" + query.toString());
        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{studentId}, (rs, rowNum) -> {
                Caregiver row = new Caregiver();
                row.setCaregiverGuid(rs.getString("PERSON_GUID"));
                row.setCaregiverSurname(rs.getString("PERSON_SURNAME"));
                row.setCaregiverGivenName(rs.getString("PERSON_GIVEN_NAME"));
                row.setRelationshipType(rs.getString("RELATIONSHIP_TYPE"));
                row.setRelationshipBloodRelative(rs.getInt("RELATIONSHIP_BLOOD_RELATIVE") == 1);
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