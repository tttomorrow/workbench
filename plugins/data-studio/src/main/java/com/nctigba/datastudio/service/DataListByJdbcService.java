package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DataListDTO;

import java.util.List;

public interface DataListByJdbcService {

    List<String> schemaListQuerySQL(String jdbcUrl, String username, String password, String sql) throws Exception;

    DataListDTO dataListQuerySQL(String jdbcUrl, String username, String password,
                                 String tableSql, String viewSql, String fun_prosSql, String sequenceSql,
                                 String synonymSql, String schema_name) throws Exception;

    List<String> databaseListQuerySQL(String jdbcUrl, String username, String password, String sql) throws Exception;

}
