wonder-lite-tests
=================
Tests that need an EOF model and a database or adaptor, salvaged from
Wonder's ERXTest and PluginTest applications. The module holds tests
only: nothing in it is packaged, installed or deployed.

EOF can be set up only once in a JVM, so the tests run in three
Surefire executions, each in a JVM of its own:

* `default-test`: ERXTest's tests, against the ERXTest model on the
  Memory adaptor, with WOUnit.
* `plugin-tests-h2`: PluginTest's tests, through H2PlugIn, against an
  in-memory H2 database.
* `plugin-tests-postgresql`: the same tests, through PostgresqlPlugIn,
  against a PostgreSQL server that Testcontainers runs in Docker.
  Without Docker, these tests are skipped.

`mvn verify` runs all three. To run one, once the module and the
frameworks are built, name its execution, as in
`mvn -pl wonder-lite-tests surefire:test@plugin-tests-h2`. In an IDE,
run the two sets of tests separately, and set the system property
`plugintest.database` to `h2` or `postgresql` for PluginTest's.

Harness
-------
ERXTest's tests extend `EOFTestCase`, and PluginTest's extend
`PluginTestCase`. Both hand out a new editing context for each
`ERXEC.newEditingContext()` call, as an app does, and dispose of them
when the test finishes. The store, or the test data, starts again for
each test, so later tests reuse global IDs, and an editing context left
for the garbage collector would release snapshots that a later test
depends on.

`EOFTestCase` loads its model once rather than for each test, as WOUnit
would: `ERXEntityClassDescription` registers class descriptions only
the first time it sees a model's name, so from the second test on, EOs
would keep the first copy's entities.
