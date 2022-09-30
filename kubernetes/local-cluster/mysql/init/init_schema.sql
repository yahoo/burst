CREATE TABLE  burst_catalog_account (
                                        pk BIGINT NOT NULL AUTO_INCREMENT,
                                        moniker VARCHAR(255) NOT NULL,
                                        labels TEXT,
                                        password VARCHAR(255),
                                        salt VARCHAR(255),
                                        PRIMARY KEY (pk),
                                        UNIQUE (moniker)
) ENGINE=InnoDb DEFAULT CHARSET=utf8;
CREATE TABLE  burst_catalog_query (
                                      pk BIGINT NOT NULL AUTO_INCREMENT,
                                      moniker VARCHAR(255) NOT NULL,
                                      labels TEXT,
                                      language_type VARCHAR(255),
                                      source TEXT,
                                      PRIMARY KEY (pk),
                                      UNIQUE (moniker)
) ENGINE=InnoDb DEFAULT CHARSET=utf8;
CREATE TABLE  burst_catalog_domain (
                                       pk BIGINT NOT NULL AUTO_INCREMENT,
                                       moniker VARCHAR(255) NOT NULL UNIQUE,
                                       labels TEXT,
                                       domain_properties TEXT,
                                       udk VARCHAR(255) UNIQUE,
                                       modify_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       create_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (pk),
                                       UNIQUE (udk)
) ENGINE=InnoDb DEFAULT CHARSET=utf8;
CREATE TABLE  burst_catalog_view (
                                     pk BIGINT NOT NULL AUTO_INCREMENT,
                                     moniker VARCHAR(255) NOT NULL,
                                     labels TEXT,
                                     domain_fk BIGINT,
                                     generation_clock BIGINT DEFAULT 0,
                                     udk VARCHAR(255),
                                     schema_name VARCHAR(255) NOT NULL,
                                     view_motif TEXT,
                                     store_properties TEXT,
                                     view_properties TEXT,
                                     modify_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
                                     create_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     access_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (pk),
                                     UNIQUE (udk, domain_fk)
) ENGINE=InnoDb DEFAULT CHARSET=utf8;
INSERT INTO burst_catalog_domain ( labels, moniker, domain_properties, udk, create_timestamp ) VALUES ( '', 'Synthetic Data', '', 'synthetic_data', 2022-09-21T15:19:37.367-05:00 );
INSERT INTO burst_catalog_view ( labels, domain_fk, generation_clock, moniker, store_properties, schema_name, view_motif, view_properties, udk ) VALUES ( '', 1, 1663791577456, 'Small Synthetic View', '', 'Unity', '', '', Some(synthetic_view_small) );
INSERT INTO burst_catalog_query (labels, moniker, language_type, source) VALUES ( '', 'Count users, sessions, events', 'Eql', 'select count(user) as users, count(user.sessions) as sessionCount, count(user.sessions.events) as eventCount
from schema unity
' );
INSERT INTO burst_catalog_query (labels, moniker, language_type, source) VALUES ( '', 'Fetch user ids', 'Eql', 'select user.id
from schema unity
' );
