reachability
============
Measures how much of wonder-lite a set of apps uses, to find code that's
safe to delete. It combines two kinds of evidence:

* **Static:** the wonder-lite classes and components that the apps name,
  and everything those reach through wonder-lite's own classes.
* **Runtime:** the classes that class-load logs show the apps loading.

Neither is complete on its own. Static analysis can't see a class name
that's built at runtime, such as `ERXMigrator`'s
`"er.extensions.migration.ERX" + adaptorName + "MigrationLock"`, and a log
shows only what a run happened to exercise. A source file that neither
covers is a candidate for deletion.

Running it
----------
It needs Python 3 and nothing else. Build wonder-lite first
(`mvn package`), because the tool reads its jars. In each app's checkout,
write out the app's runtime classpath:

```
mvn dependency:build-classpath -Dmdep.includeScope=runtime -Dmdep.outputFile=/elsewhere/app.txt
```

Then run the tool on the apps:

```
python3 tools/reachability/reachability.py /path/to/app... \
    --classpath /elsewhere/app.txt... --log /elsewhere/classload.log... \
    --details /elsewhere/details.tsv
```

It prints a table of the lines of Java in each framework that each kind
of evidence covers. Keep what it reads and writes about the apps out of
this repository.

* `APP`: an app's checkout. Its sources, resources and templates are
  scanned.
* `--classpath`: an app's runtime classpath. The wonder-lite frameworks on
  it count as in use, whatever their version. Other frameworks on it
  (jars with a `Resources/Info.plist`) are scanned like the app. A class
  that a jar on it provides, when the app lacks the wonder-lite framework
  that has the class, doesn't count. Without `--classpath`, the app's POM
  says which frameworks it uses, and its other frameworks go unscanned.
* `--jar`: a jar or a directory of classes to scan as well.
* `--log`: a class-load log, from `-Xlog:class+load` (Java 9 and later) or
  `-verbose:class` (Java 8). A class counts if it loaded from the jar of
  the framework that has it, whatever the version, so logs from apps still
  on Wonder 7.4 count too.
* `--details`: also writes a line per source file: framework, file, lines,
  whether static evidence and the logs cover it, and, if it's reached
  statically, the chain that reaches it and where the apps name the start
  of that chain.

What counts
-----------
The apps name a class or component:

* In Java: by qualified name, imports included, by simple name under an
  on-demand import (`import er.extensions.eof.*`), or, for a component,
  by name in a string, as in `pageWithName("…")`.
* In Properties, models and other text resources: by qualified name.
  Comments don't count.
* In templates (`.wod` and `.html`): as an element type.
* In the class files of the other frameworks they use: by reference, or
  in a string, as in Java.

From there, the classes reached are:

* The classes that a class's constant pool refers to, in class entries,
  descriptors and generic signatures.
* The classes that its string constants name, as in Java.
* A nested class's enclosing class.
* The elements in a component's template.
* For a framework in use: its principal class, the frameworks it depends
  on, and the classes its resources name. Of its Properties, only values
  count, because the keys are defaults for every feature.

Lines are all the lines in a source file, comments and blank lines
included.
