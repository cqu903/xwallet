package com.zerofinance.xwallet.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class TestProfileSchemaIsolationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldNotLoadCollectionSchemaForSharedTestProfile() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'COLLECTION_TASK'",
                Integer.class);

        assertEquals(0, tableCount);
    }
}
