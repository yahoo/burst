CREATE TABLE  burst_catalog_worker (
    pk BIGINT NOT NULL AUTO_INCREMENT,
    labels TEXT,
    host_name VARCHAR(255) NOT NULL UNIQUE,
    moniker VARCHAR(255) NOT NULL UNIQUE,
    worker_properties TEXT,
    PRIMARY KEY (pk),
    UNIQUE (moniker)
)
