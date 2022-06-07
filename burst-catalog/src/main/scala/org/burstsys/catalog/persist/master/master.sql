CREATE TABLE  burst_catalog_master (
    pk BIGINT NOT NULL AUTO_INCREMENT,
    host_name VARCHAR(255) NOT NULL UNIQUE,
    labels TEXT,
    moniker VARCHAR(255) NOT NULL UNIQUE,
    master_properties TEXT,
    PRIMARY KEY (pk),
    UNIQUE (moniker)
)
