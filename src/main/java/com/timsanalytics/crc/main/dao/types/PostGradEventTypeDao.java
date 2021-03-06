package com.timsanalytics.crc.main.dao.types;

import com.timsanalytics.crc.main.beans.types.PostGradEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostGradEventTypeDao {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final JdbcTemplate mySqlAuthJdbcTemplate;

    @Autowired
    public PostGradEventTypeDao(JdbcTemplate mySqlAuthJdbcTemplate) {
        this.mySqlAuthJdbcTemplate = mySqlAuthJdbcTemplate;
    }

    public List<PostGradEventType> getPostGradEventTypeList() {
        StringBuilder query = new StringBuilder();
        query.append("  SELECT\n");
        query.append("      post_grad_event_type_id,\n");
        query.append("      post_grad_event_type_name\n");
        query.append("  FROM\n");
        query.append("      CRC.Ref_Post_Grad_Event_Type\n");
        query.append("  WHERE\n");
        query.append("      deleted = 0\n");
        query.append("  ORDER BY\n");
        query.append("      post_grad_event_type_name\n");
        this.logger.trace("SQL:\n" + query.toString());
        try {
            return this.mySqlAuthJdbcTemplate.query(query.toString(), new Object[]{}, (rs, rowNum) -> {
                PostGradEventType row = new PostGradEventType();
                row.setPostGradEventTypeId(rs.getInt("post_grad_event_type_id"));
                row.setPostGradEventTypeName(rs.getString("post_grad_event_type_name"));
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
