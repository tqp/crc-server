package com.timsanalytics.crc.auth.authCommon.dao;

import com.timsanalytics.crc.auth.authCommon.beans.Role;
import com.timsanalytics.crc.auth.authCommon.beans.User;
import com.timsanalytics.crc.auth.authCommon.dao.rowMappers.RoleRowMapper;
import com.timsanalytics.crc.auth.authCommon.dao.rowMappers.UserRowMapper;
import com.timsanalytics.crc.common.beans.KeyValue;
import com.timsanalytics.crc.common.beans.ServerSidePaginationRequest;
import com.timsanalytics.crc.main.dao.UtilsDao;
import com.timsanalytics.crc.utils.BCryptEncoderService;
import com.timsanalytics.crc.utils.PrintObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserDao {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final BCryptEncoderService bCryptEncoderService;
    private final UtilsDao utilsDao;

    @Qualifier("mySqlAuthJdbcTemplate")
    private final JdbcTemplate mySqlAuthJdbcTemplate;

    public UserDao(BCryptEncoderService bCryptEncoderService,
                   JdbcTemplate mySqlAuthJdbcTemplate,
                   UtilsDao utilsDao) {
        this.mySqlAuthJdbcTemplate = mySqlAuthJdbcTemplate;
        this.bCryptEncoderService = bCryptEncoderService;
        this.utilsDao = utilsDao;
    }

    public User createUser(User user, User loggedInUser) {
        this.logger.debug("UserDao -> createUser");
        StringBuilder query = new StringBuilder();
        query.append("  INSERT INTO\n");
        query.append("      CRC.Auth_User\n");
        query.append("      (\n");
        query.append("          username,\n");
        query.append("          surname,\n");
        query.append("          given_name,\n");
        query.append("          position,\n");
        query.append("          password,\n");
        query.append("          password_set,\n");
        query.append("          created_on,\n");
        query.append("          created_by,\n");
        query.append("          updated_on,\n");
        query.append("          updated_by,\n");
        query.append("          deleted\n");
        query.append("      )\n");
        query.append("      VALUES\n");
        query.append("      (\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          ?,\n");
        query.append("          NOW(),\n");
        query.append("          ?,\n");
        query.append("          NOW(),\n");
        query.append("          ?,\n");
        query.append("          0\n");
        query.append("      )\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("Username: " + user.getUsername());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, user.getUsername());
                        ps.setString(2, user.getSurname());
                        ps.setString(3, user.getGivenName());
                        ps.setInt(4, user.getPosition());
                        ps.setString(5, user.getPassword() != null ? this.bCryptEncoderService.encode(user.getPassword()) : null);
                        ps.setString(6, user.getPassword() != null ? now.toString() : null);
                        ps.setInt(7, -1);
                        ps.setInt(8, -1);
                        return ps;
                    });
            int lastInsertId = this.utilsDao.getLastInsertId();
            this.logger.debug("New User ID: " + lastInsertId);
            return this.getUserDetail(lastInsertId);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User getUserDetail(Integer userId) {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      user_id,\n");
        query.append("      username,\n");
        query.append("      password,\n");
        query.append("      password_set,\n");
        query.append("      password_reset,\n");
        query.append("      last_login,\n");
        query.append("      login_count,\n");
        query.append("      surname,\n");
        query.append("      given_name,\n");
        query.append("      position,\n");
        query.append("      user_profile_photo_url,\n");
        query.append("      created_on,\n");
        query.append("      created_by,\n");
        query.append("      updated_on,\n");
        query.append("      updated_by,\n");
        query.append("      deleted\n");
        query.append("  FROM\n");
        query.append("      CRC.Auth_User\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userId: " + userId);
        try {
            return this.mySqlAuthJdbcTemplate.queryForObject(query.toString(), new Object[]{userId}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public List<User> getUserList() {
        this.logger.debug("UserDao -> getUserList");
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      user_id,\n");
        query.append("      username,\n");
        query.append("      surname,\n");
        query.append("      given_name\n");
        query.append("      password,\n");
        query.append("      password_set,\n");
        query.append("      password_reset,\n");
        query.append("      last_login,\n");
        query.append("      login_count,\n");
        query.append("      user_profile_photo_url,\n");
        query.append("      created_on,\n");
        query.append("      created_by,\n");
        query.append("      updated_on,\n");
        query.append("      updated_by,\n");
        query.append("      deleted\n");
        query.append("  FROM\n");
        query.append("      CRC.Auth_User\n");
        query.append("  WHERE\n");
        query.append("      deleted = 0\n");
        query.append("  ORDER BY\n");
        query.append("      surname,\n");
        query.append("      given_name\n");
        this.logger.debug("SQL:\n" + query.toString());
        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public List<User> getUserList_SSP(ServerSidePaginationRequest<User> serverSidePaginationRequest) {
        int pageStart = (serverSidePaginationRequest.getPageIndex()) * serverSidePaginationRequest.getPageSize();
        int pageSize = serverSidePaginationRequest.getPageSize();

        String defaultSortField = "username";
        String sortColumn = serverSidePaginationRequest.getSortColumn() != null ? serverSidePaginationRequest.getSortColumn() : defaultSortField;
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
        query.append(getUserList_SSP_RootQuery(serverSidePaginationRequest));
        query.append("          ) AS ROOT_QUERY\n");
        query.append("          -- END ROOT QUERY\n");

        query.append("          ORDER BY\n");
        query.append("              "); // Spacing for output
        query.append(sortColumn).append(" ").append(sortDirection.toUpperCase()).append(",\n");
        query.append("              surname,\n");
        query.append("              given_name\n");
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
                User row = new User();
                row.setUserId(rs.getInt("user_id"));
                row.setUsername(rs.getString("username"));
                row.setSurname(rs.getString("surname"));
                row.setGivenName(rs.getString("given_name"));
                row.setLastLogin(rs.getString("last_login"));
                row.setLoginCount(rs.getInt("login_count"));
                // Roles
                row.setRoleView(rs.getInt("role_view"));
                row.setRoleManager(rs.getInt("role_manager"));
                row.setRoleCaseManager(rs.getInt("role_case_manager"));
                row.setRoleCreate(rs.getInt("role_create"));
                row.setRoleEdit(rs.getInt("role_edit"));
                row.setRoleReports(rs.getInt("role_reports"));
                row.setRoleFinance(rs.getInt("role_finance"));
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

    private String getUserList_SSP_RootQuery(ServerSidePaginationRequest<User> serverSidePaginationRequest) {
        //noinspection StringBufferReplaceableByString
        StringBuilder query = new StringBuilder();
        query.append("              SELECT\n");
        query.append("                  Auth_User.user_id,\n");
        query.append("                  username,\n");
        query.append("                  surname,\n");
        query.append("                  given_name,\n");
        query.append("                  last_login,\n");
        query.append("                  login_count,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 1 THEN 1 ELSE NULL END) AS role_view,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 4 THEN 1 ELSE NULL END) AS role_manager,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 5 THEN 1 ELSE NULL END) AS role_case_manager,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 9 THEN 1 ELSE NULL END) AS role_create,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 10 THEN 1 ELSE NULL END) AS role_edit,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 11 THEN 1 ELSE NULL END) AS role_reports,\n");
        query.append("                  SUM(CASE WHEN Auth_User_Role.role_id = 12 THEN 1 ELSE NULL END) AS role_finance\n");
        query.append("              FROM\n");
        query.append("                  CRC.Auth_User\n");
        query.append("                  LEFT JOIN CRC.Auth_User_Role ON Auth_User_Role.user_id = Auth_User.user_id AND Auth_User_Role.deleted = 0\n");
        query.append("              WHERE\n");
        query.append("              (\n");
        query.append("                  Auth_User.deleted = 0\n");
        query.append("                  AND ");
        query.append(getStudentList_SSP_AdditionalWhereClause(serverSidePaginationRequest));
        query.append("              )\n");
        query.append("              GROUP BY\n");
        query.append("                  username\n");
        return query.toString();
    }

    private String getStudentList_SSP_AdditionalWhereClause(ServerSidePaginationRequest<User> serverSidePaginationRequest) {
        StringBuilder whereClause = new StringBuilder();
        String nameFilter = serverSidePaginationRequest.getNameFilter() != null ? serverSidePaginationRequest.getNameFilter() : "";

        // NAME FILTER CLAUSE
        if (!"".equalsIgnoreCase(nameFilter)) {
            whereClause.append("                  (\n");
            whereClause.append("                    UPPER(Auth_User.surname) LIKE UPPER('%").append(nameFilter).append("%')\n");
            whereClause.append("                    OR\n");
            whereClause.append("                    UPPER(Auth_User.given_name) LIKE UPPER('%").append(nameFilter).append("%')\n");
            whereClause.append("                    OR\n");
            whereClause.append("                    UPPER(Auth_User.username) LIKE UPPER('%").append(nameFilter).append("%')\n");
            whereClause.append("                  )\n");
        } else {
            whereClause.append("                  (1=1)\n");
        }

        return whereClause.toString();
    }

    public int getUserList_SSP_TotalRecords(ServerSidePaginationRequest<User> serverSidePaginationRequest) {
        StringBuilder query = new StringBuilder();
        query.append("          SELECT\n");
        query.append("              COUNT(*)\n");
        query.append("          FROM\n");
        query.append("          -- ROOT QUERY\n");
        query.append("          (\n");
        query.append(getUserList_SSP_RootQuery(serverSidePaginationRequest));
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

    public User updateUser(User User, User loggedInUser) {
        this.logger.debug("UserDao -> updateUser");
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Auth_User\n");
        query.append("  SET\n");
        query.append("      username = ?,\n");
        query.append("      surname = ?,\n");
        query.append("      given_name = ?,\n");
        query.append("      position = ?,\n");
        query.append("      updated_on = NOW(),\n");
        query.append("      updated_by = ?\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
//                        ps.setInt(1, loggedInUser.getUsername());
                        ps.setString(1, User.getUsername());
                        ps.setString(2, User.getSurname());
                        ps.setString(3, User.getGivenName());
                        ps.setInt(4, User.getPosition());
                        ps.setInt(5, -1);
                        ps.setInt(6, User.getUserId());
                        return ps;
                    }
            );
            return this.getUserDetail(User.getUserId());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User deleteUser(Integer userId) {
        this.logger.debug("UserDao -> deleteUser: " + userId);
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Auth_User\n");
        query.append("  SET\n");
        query.append("      deleted = 1\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userId: " + userId);
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setInt(1, userId);
                        return ps;
                    }
            );
            return this.getUserDetail(userId);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User purgeUser(Integer userId) {
        this.logger.debug("UserDao -> purgeUser: " + userId);
        StringBuilder query = new StringBuilder();
        query.append("  DELETE FROM\n");
        query.append("      CRC.App_User\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userId: " + userId);
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setInt(1, userId);
                        return ps;
                    }
            );
            return this.getUserDetail(userId);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    // RELATED CRUD

    public User disableUser(Integer userId) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      USER\n");
        query.append("  SET\n");
        query.append("      USER.STATUS = 'Disabled'\n");
        query.append("  WHERE\n");
        query.append("      USER.USER_GUID = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userGuid: " + userId);
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setInt(1, userId);
                        return ps;
                    }
            );
            return this.getUserDetail(userId);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User enableUser(Integer userId) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      USER\n");
        query.append("  SET\n");
        query.append("      USER.STATUS = 'Active'\n");
        query.append("  WHERE\n");
        query.append("      USER.USER_GUID = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userId: " + userId);
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setInt(1, userId);
                        return ps;
                    }
            );
            return this.getUserDetail(userId);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User updateMyProfile(User User, User loggedInUser) {
        this.logger.debug("UserDao -> updateMyProfile: userGuid=" + loggedInUser.getUserId());
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      USER\n");
        query.append("  SET\n");
        query.append("      USER.UPDATED_ON = NOW(),\n");
        query.append("      USER.UPDATED_BY = ?\n");
        query.append("  WHERE\n");
        query.append("      USER.USER_GUID = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("theme: " + User.getTheme());
        this.logger.debug("userGuid: " + User.getUserId());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setInt(1, loggedInUser.getUserId());
                        ps.setInt(2, User.getUserId());
                        return ps;
                    }
            );
            return this.getUserDetail(User.getUserId());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    // NON-CRUD DAOs

    public User updatePassword(User user, User loggedInUser) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Auth_User\n");
        query.append("  SET\n");
        query.append("      password = ?,\n");
        query.append("      password_set = NOW(),\n");
        query.append("      password_reset = 0,\n");
        query.append("      updated_on = NOW(),\n");
        query.append("      updated_by = ?\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userGuid: " + user.getUserId());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setString(1, this.bCryptEncoderService.encode(user.getPassword()));
                        ps.setInt(2, -1);
                        ps.setInt(3, user.getUserId());
                        return ps;
                    }
            );
            return this.getUserDetail(user.getUserId());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User resetPassword(User user, User loggedInUser) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Auth_User\n");
        query.append("  SET\n");
        query.append("      password = ?,\n");
        query.append("      password_set = NULL,\n");
        query.append("      password_reset = 1,\n");
        query.append("      updated_on = NOW(),\n");
        query.append("      updated_by = ?\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userGuid: " + user.getUserId());
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setString(1, this.bCryptEncoderService.encode(user.getPassword()));
                        ps.setInt(2, -1);
                        ps.setInt(3, user.getUserId());
                        return ps;
                    }
            );
            return this.getUserDetail(user.getUserId());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public User getUserDetailByUsername(String username) {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      user_id,\n");
        query.append("      username,\n");
        query.append("      password,\n");
        query.append("      password_set,\n");
        query.append("      password_reset,\n");
        query.append("      last_login,\n");
        query.append("      login_count,\n");
        query.append("      surname,\n");
        query.append("      given_name,\n");
        query.append("      position,\n");
        query.append("      user_profile_photo_url,\n");
        query.append("      created_on,\n");
        query.append("      created_by,\n");
        query.append("      updated_on,\n");
        query.append("      updated_by,\n");
        query.append("      deleted\n");
        query.append("  FROM\n");
        query.append("      CRC.Auth_User\n");
        query.append("  WHERE\n");
        query.append("      username = ?\n");
        query.append("      AND deleted = 0\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userId: " + username);
        try {
            return this.mySqlAuthJdbcTemplate.queryForObject(query.toString(), new Object[]{username}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            //this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public void updateScreenResolution(String resolution, User loggedInUser) {
        StringBuilder query = new StringBuilder();
        query.append("  UPDATE\n");
        query.append("      CRC.Auth_User\n");
        query.append("  SET\n");
        query.append("      screen_resolution = ?\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        try {
            this.mySqlAuthJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(query.toString());
                        ps.setString(1, resolution);
                        ps.setInt(2, loggedInUser.getUserId());
                        return ps;
                    }
            );
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
        }
    }

    public Integer getUserIdByUsername(String username) {
        this.logger.debug("UserDao -> getUserIdByUsername: username=" + username);
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      user_id\n");
        query.append("  FROM\n");
        query.append("      CRC.Auth_User\n");
        query.append("  WHERE\n");
        query.append("      username = ?\n");
        query.append("      AND deleted = 0\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("username: " + username);
        try {
            return this.mySqlAuthJdbcTemplate.queryForObject(query.toString(), new Object[]{username}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

    public List<Role> getUserRoles(Integer userGuid) {
        this.logger.debug("UserDao -> getUserRoles: userGuid=" + userGuid);
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      Auth_User_Role.role_id,\n");
        query.append("      role_name,\n");
        query.append("      authority\n");
        query.append("  FROM\n");
        query.append("      CRC.Auth_User_Role\n");
        query.append("      LEFT JOIN CRC.Auth_Role ON Auth_Role.role_id = Auth_User_Role.role_id AND Auth_Role.deleted = 0\n");
        query.append("  WHERE\n");
        query.append("      user_id = ?\n");
        query.append("      AND Auth_User_Role.deleted = 0\n");
        this.logger.debug("SQL:\n" + query.toString());
        this.logger.debug("userGuid: " + userGuid);
        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{userGuid}, new RoleRowMapper());
        } catch (EmptyResultDataAccessException e) {
            this.logger.error("EmptyResultDataAccessException: " + e);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
            return null;
        }
    }

}
