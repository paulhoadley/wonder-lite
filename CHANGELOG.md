Changelog
=========

All notable changes to Wonder Lite are recorded here. Wonder Lite has
its own version line (see the README), and any 0.x release may break
compatibility with the one before it.

0.2 — 2026-10-04
----------------
The first app runs on Wonder Lite. A production app has moved from
Wonder 7.4 to this release and runs in staging ([#18]). This release
adds what that took: the in-memory EOF adaptor that WOUnit tests need,
and a BOM for apps to import.

### Added

- JavaMemoryAdaptor, the in-memory EOF adaptor that WOUnit depends
  on, is back. Unlike Wonder's builds, its bundle declares
  `EOAdaptorClassName`, so EOF finds the `Memory` adaptor without
  WOUnit's workaround ([#15]).
- A BOM, `net.logicsquad.wonder:wonder-lite-bom`. Importing it
  manages the versions of every framework and of the ERFoundation
  and ERWebObjects jars, and nothing else ([#17]).

### Fixed

- Sources jars now hold only sources. They used to include each
  framework's resources under paths starting with `../`, which some
  tools and repositories refuse ([#39]).

### Known issues

None of 0.1's known issues is fixed yet:

- `NSTimeZone.systemTimeZone()` fails on JDK 24 and later ([#27]).
- Bundles report `CFBundleIdentifier` as `com.apple.myapp`, because
  wolifecycle-maven-plugin 2.5 can't set it.
- `jsonStringFromPropertyList()` can write invalid JSON escapes
  ([#31]).
- Tests that need an EOF model have not been ported yet ([#20]).
- Releases are not yet published to a Maven repository ([#16]).

0.1 — 2026-10-01
----------------
The first release. It sets a trustworthy baseline: the build, the
tests and the WOLips project setup can all be relied on. The
starting point is Wonder's `master` branch as of 4 January 2026
(commit `da13591`, which is Wonder 7.4 plus 15 small commits).

### Coming from Wonder 7.4

- **New coordinates.** Group ID `net.logicsquad.wonder`, version `0.1`,
  with all frameworks sharing one version ([#2]).
- **Java 21** is the baseline. JDK 25 is built in CI but not yet
  supported ([#13]).
- **13 frameworks remain:** Ajax, ERAttachment, ERAttributeExtension,
  ERExtensions, ERJavaMail, ERJGroupsSynchronizer, ERPDFGeneration,
  ERPrototypes, ERRest, H2PlugIn, JavaWOExtensions, PostgresqlPlugIn
  and WOOgnl. Everything else is gone, including D2W, ERChronic,
  ExcelGenerator, ERCoolComponents ([#14]), the Microsoft SQL Server
  plug-in and all of Wonder's applications, examples and utilities.
- Requires `wonder.core:ERFoundation` 1.2 (up from 1.1) and
  `wonder.core:ERWebObjects` 1.0 ([#12]).

### Removed

- The dependency on JavaXML.framework.
  `ERXPropertyListSerialization`'s `convertDOMToString()` now uses the
  standard `javax.xml` classes rather than Xerces. Its output differs
  only in indentation.
- The dependency on JavaWebObjects.framework.
- Servlet deployment support: `ERXServletApplication`,
  `ERXServletAdaptor` and `_WOApplicationWrapper`.
- `ERXMoney` and its enums (`ERXMoneyEnums`, `ERXContinentEnums`,
  `ERXEuropeanUnionsEnums`).
- Unused classes including `ERXTcpIp`, the `ERXHttp*Data` classes,
  `ERXAccessPermission`, `ERXVirusScanner` and
  `ERXClamAvVirusScanner`, `WOWebServicePatch`, the `er.testrunner`
  package and `ERXWOTestInterface`.
- ERAttachment's ImageMagick command-line metadata parser.
- `ERXClippy` (a Flash component) and other dead files ([#10]).
- Dependencies ([#9]):
  - JUnit and Hamcrest are now test-scoped, so they no longer ship to
    apps.
  - ERJavaMail drops `com.sun.mail:dsn`. Add it yourself if you parse
    delivery-status reports over IMAP.
  - ERPDFGeneration drops `flying-saucer-swt` and now declares
    `openpdf` directly.

### Changed

- Script tags written by `ERXJavaScript` and `ERXRedirect` no longer
  carry `type="text/javascript"`. To restore it, set
  `er.extensions.ERXResponseRewriter.javascriptTypeAttribute=true`
  ([#12], from wocommunity/wonder#1021).
- WOOgnl's helper-function parser logs through SLF4J rather than
  Log4J.
- WOOgnl no longer reads `ognl.helperFunctions`. The parser is active
  unless `ognl.active` is false ([#11]).

### Added

- `AjaxUpdateContainer` takes `role`, `aria-live`, `aria-atomic`,
  `aria-busy` and `aria-relevant` bindings ([#12], from
  wocommunity/wonder#1022).
- `ERXJavaScript` takes a `type` binding ([#12]).
- READMEs for Ajax, ERJavaMail, ERPDFGeneration, PostgresqlPlugIn and
  WOOgnl, rewritten from the old `src/doc` folders ([#11]).
- A top-level `LICENSE` (the NetStruxr Public Software License)
  ([#7]).

### Fixed

- `ERXFileUtilities.chmod()` and `chmodRecursively()` now wait for
  `chmod` to finish, and throw `IOException` if it fails. Previously
  the process could be destroyed before it ran ([#5]).
- WOLips builds in Eclipse now include each framework's components and
  web-server resources ([#3]).
- ERJavaMail ships `META-INF/javamail.providers` again ([#4]).

### Build and tests

- Unit tests now run in the Maven build ([#4]). 31 test classes from
  Wonder's ERXTest application now run in ERExtensions, bringing it to
  625 tests ([#1], [#5]).
- CI builds and tests on every push to `develop` and `main`, on JDK 21
  and JDK 25 ([#6]).
- A cleaned-up root POM with current Maven plugin versions ([#8]).

### Known issues

- `NSTimeZone.systemTimeZone()` fails on JDK 24 and later ([#27]).
- Bundles report `CFBundleIdentifier` as `com.apple.myapp`, because
  wolifecycle-maven-plugin 2.5 can't set it.
- `jsonStringFromPropertyList()` can write invalid JSON escapes
  ([#31]).
- Tests that need an EOF model have not been ported yet ([#20]).
- Releases are not yet published to a Maven repository ([#16]).

[#1]: https://github.com/paulhoadley/wonder-lite/issues/1
[#2]: https://github.com/paulhoadley/wonder-lite/issues/2
[#3]: https://github.com/paulhoadley/wonder-lite/issues/3
[#4]: https://github.com/paulhoadley/wonder-lite/issues/4
[#5]: https://github.com/paulhoadley/wonder-lite/issues/5
[#6]: https://github.com/paulhoadley/wonder-lite/issues/6
[#7]: https://github.com/paulhoadley/wonder-lite/issues/7
[#8]: https://github.com/paulhoadley/wonder-lite/issues/8
[#9]: https://github.com/paulhoadley/wonder-lite/issues/9
[#10]: https://github.com/paulhoadley/wonder-lite/issues/10
[#11]: https://github.com/paulhoadley/wonder-lite/issues/11
[#12]: https://github.com/paulhoadley/wonder-lite/issues/12
[#13]: https://github.com/paulhoadley/wonder-lite/issues/13
[#14]: https://github.com/paulhoadley/wonder-lite/issues/14
[#15]: https://github.com/paulhoadley/wonder-lite/issues/15
[#16]: https://github.com/paulhoadley/wonder-lite/issues/16
[#17]: https://github.com/paulhoadley/wonder-lite/issues/17
[#18]: https://github.com/paulhoadley/wonder-lite/issues/18
[#20]: https://github.com/paulhoadley/wonder-lite/issues/20
[#27]: https://github.com/paulhoadley/wonder-lite/issues/27
[#31]: https://github.com/paulhoadley/wonder-lite/issues/31
[#39]: https://github.com/paulhoadley/wonder-lite/issues/39
