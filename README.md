![](https://github.com/paulhoadley/wonder-lite/workflows/build/badge.svg)

Wonder Lite
===========

About
-----
This is an experimental and _highly_ pared-down version of [Project
Wonder](https://github.com/wocommunity/wonder).

Dependencies
------------
Wonder Lite still has some, uh, _unusual_ dependencies that are going
to take some thought to excise.

* `wonder.core.ERFoundation-1.2`
* `wonder.core.ERWebObjects-1.0`

Both of these are currently available from the WOCommunity Maven
repository.

Versioning
----------
Wonder Lite has its own group ID (`net.logicsquad.wonder`) and its own
version line, starting at 0.1. It doesn't continue Wonder's 7.x.

* All frameworks share one version, set in the root POM.
* Any 0.x release may break compatibility with the one before it, so
  pin an exact version.
* 1.0 will be the first release we'd run in production.

The starting point is Wonder's `master` branch as of 4 January 2026,
commit
[`da13591`](https://github.com/wocommunity/wonder/commit/da1359157a3433fabd64e739af64b3904e4547b9),
which is Wonder 7.4 plus 15 small commits.

Licence
-------
Wonder Lite is derived from Project Wonder and keeps its licence, the
NetStruxr Public Software License, a BSD-style licence: see
[LICENSE](LICENSE). ERJavaMail began as Camille Troillard's Odaiko
MailDelivery framework, whose README says it's under the GNU Lesser
General Public License: see [ERJavaMail/README.md](ERJavaMail/README.md).
Bundled third-party files, such as the JavaScript libraries in Ajax,
carry their own licences.

FAQs
----
* _My favourite framework/feature is missing—where did it go?_ It was
  deleted. It won't be coming back.
* _Why isn't this project a fork of Project Wonder?_ Wonder has a
  long, complicated history, stretching back at least to 2001, none of
  which is particularly relevant to this project. That history is
  always available in Wonder's repoitory.
* _Then why is the initial clone almost 200M?_ Although the full
  history isn't relevant, I thought it was important to show how we
  got here. So the initial commit is basically Wonder's `master`
  branch as it appeared on 4 January 2026, followed by a number of
  extensive deletes and some reorganisation.
