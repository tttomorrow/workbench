package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseMetaarrayQuery {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String objectType;
}
