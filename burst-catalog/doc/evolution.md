![Burst](../../documentation/burst_h_small.png "") ![catalog](catalog_small.png "")

# Schema Evolution


This is a record of the ```ALTER TABLE``` commands needed to migrate the
catalog schema from one version to another.  The changes are recorded by
date

#### September 8, 2017

The schema name field was migrated from the domain object to the view
object so different views in the same domain could have different schemas.
Timestamp fields were also added to record a times of interesting events.

```mysql-sql
# 
alter table burst_catalog_domain 
            drop schema_name, 
            add modify_timestamp timestamp default current_timestamp on update current_timestamp, 
            add create_timestamp timestamp;
            
alter table burst_catalog_view 
            add schema_name varchar(255) not null, 
            add modify_timestamp timestamp default current_timestamp on update current_timestamp, 
            add create_timestamp timestamp, 
            add access_timestamp timestamp;
```

#### October 8, 2017

Idempotent adding of project id label to domains from the domain property:
```mysql-sql
update burst_catalog_domain d 
       set d.labels = CONCAT(d.labels,'project_id=',
                      SUBSTRING_INDEX(SUBSTRING_INDEX(d.domain_properties, 'project_id=', -1), ';', 1),';') 
       where d.domain_properties like '%project_id%' and d.labels not like '%project_id%';
```

add the generations field to views:
```mysql-sql
alter table burst_catalog_view add generation_clock BIGINT NOT NULL DEFAULT 0 after domain_fk;
```

---
------ [HOME](../readme.md) --------------------------------------------
