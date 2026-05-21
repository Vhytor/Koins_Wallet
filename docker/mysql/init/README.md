# MySQL init scripts

Place any SQL files you want executed at container initialization in this folder. Files will be copied into `/docker-entrypoint-initdb.d` in the MySQL container and executed on first startup.

For example, you can add `01_schema.sql` and `02_seed.sql` with DDL and sample data.

