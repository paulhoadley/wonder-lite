PostgresqlPlugIn
================
A plugin for WebObjects' JDBC adaptor that lets EOF work with
PostgreSQL. The PostgreSQL JDBC driver (`org.postgresql:postgresql`)
isn't included, so apps need it on their classpath too.

Connection dictionary
---------------------
* URL: `jdbc:postgresql://databaseserver/databasename`
* driver: `org.postgresql.Driver`
* plugin: `PostgresqlPlugIn`

When generating SQL for a schema, EOF normally connects to the
database server to find out which types it supports. To avoid that,
for example when the server isn't reachable, add
`?useBundledJdbcInfo=true` to the URL and the plugin uses the type
information it bundles (from PostgreSQL 8.2).

Primary keys
------------
Primary keys come from a sequence named after the entity's primary key
root name plus `_seq`. The plugin creates the sequence if it doesn't
exist.
