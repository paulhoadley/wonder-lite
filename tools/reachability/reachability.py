#!/usr/bin/env python3
"""Measures how much of wonder-lite the given apps use.

Static evidence: starting from the wonder-lite classes and components that the apps
name, follow the references between wonder-lite's classes. Runtime evidence: the
classes that class-load logs show loaded. For each framework, the report gives the
lines of Java that each kind of evidence covers, and --details lists every source
file. A file that neither covers is a candidate for deletion. See README.md.
"""
import argparse
import os
import re
import struct
import sys
import zipfile
from collections import defaultdict, deque

# A Java class name, with a package, possibly followed by nested class names
QUALIFIED_NAME = re.compile(r'(?<![\w.$])((?:[a-z_][\w$]*\.)+[A-Z][\w$]*(?:\.[A-Z][\w$]*)*)')
# Element types in .wod files and in inline <wo:...> tags
WOD_ELEMENT = re.compile(r'^\s*[\w$]+\s*:\s*([\w.$]+)\s*\{', re.M)
INLINE_ELEMENT = re.compile(r'<wo:([\w.$]+)', re.I)
JAVA_ON_DEMAND_IMPORT = re.compile(r'^\s*import\s+([\w.]+)\.\*\s*;', re.M)
JAVA_STRING = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
JAVA_IDENTIFIER = re.compile(r'\b[A-Z][\w$]*\b')
# Class names in descriptors and signatures in a class file's constant pool
DESCRIPTOR_CLASS = re.compile(r'L([\w/$]+)[;<]')
PRINCIPAL_CLASS = re.compile(r'<key>NSPrincipalClass</key>\s*<string>([^<]*)</string>')

TEMPLATE_SUFFIXES = ('.wod', '.html', '.xhtml')
TEXT_SUFFIXES = ('.properties', '.plist', '.fspec', '.eomodeld', '.xml', '.d2wmodel', '.strings', '.json', '.yml', '.yaml')
SKIPPED_DIRECTORIES = {'target', 'build', 'bin', 'dist', 'node_modules'}


def parse_class(data):
    """Returns the classes a class file refers to, and its string constants."""
    count = struct.unpack_from('>H', data, 8)[0]
    utf8 = {}
    class_entries = []
    string_entries = []
    i, index = 10, 1
    while index < count:
        tag = data[i]
        if tag == 1:
            length = struct.unpack_from('>H', data, i + 1)[0]
            utf8[index] = data[i + 3:i + 3 + length].decode('utf-8', 'replace')
            i += 3 + length
        elif tag == 7:
            class_entries.append(struct.unpack_from('>H', data, i + 1)[0])
            i += 3
        elif tag == 8:
            string_entries.append(struct.unpack_from('>H', data, i + 1)[0])
            i += 3
        elif tag in (16, 19, 20):
            i += 3
        elif tag == 15:
            i += 4
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            i += 5
        elif tag in (5, 6):
            # Longs and doubles take two entries
            i += 9
            index += 1
        else:
            raise ValueError('unknown constant pool tag %d' % tag)
        index += 1
    references = set()
    for entry in class_entries:
        name = utf8[entry].lstrip('[')
        if name.startswith('L') and name.endswith(';'):
            name = name[1:-1]
        references.add(name.replace('/', '.'))
    for text in utf8.values():
        for match in DESCRIPTOR_CLASS.finditer(text):
            references.add(match.group(1).replace('/', '.'))
    return references, [utf8[entry] for entry in string_entries]


def read_text(data):
    return data.decode('utf-8', 'replace')


def read_file(path):
    with open(path, 'rb') as f:
        return f.read()


def is_template(name):
    return name.endswith(TEMPLATE_SUFFIXES)


def properties_text(text, values_only):
    """The text of a Properties file without its comments, and perhaps without its keys."""
    lines = [line for line in text.splitlines() if not line.lstrip().startswith(('#', '!'))]
    if values_only:
        lines = [re.split(r'[=:]', line, 1)[-1] for line in lines]
    return '\n'.join(lines)


def is_properties(name):
    return name.rsplit('/', 1)[-1] == 'Properties' or name.endswith('.properties')


def is_text_resource(name):
    return is_properties(name) or name.endswith(TEXT_SUFFIXES)


class WonderLite:
    """wonder-lite's frameworks and classes, and the references between them."""

    def __init__(self, root):
        self.root = root
        self.version, self.modules = self._read_modules()
        self.module_of = {}
        self.edges = defaultdict(set)
        self.by_simple_name = defaultdict(set)
        self.components = {}
        self.source_file = {}
        self.lines = {}
        jars = {}
        for module in self.modules:
            jars[module] = zipfile.ZipFile(os.path.join(root, module, 'target', '%s-%s.jar' % (module, self.version)))
            for entry in jars[module].namelist():
                if entry.endswith('.class') and not entry.endswith('module-info.class'):
                    name = entry[:-6].replace('/', '.')
                    self.module_of[name] = module
                    if '$' not in name:
                        self.by_simple_name[name.rsplit('.', 1)[-1]].add(name)
        for jar in jars.values():
            for entry in jar.namelist():
                if '.wo/' in entry:
                    self.component_node(entry.split('.wo/', 1)[0].rsplit('/', 1)[-1])
        for module, jar in jars.items():
            framework = self.framework_node(module)
            for entry in jar.namelist():
                data = jar.read(entry)
                if entry.endswith('.class') and not entry.endswith('module-info.class'):
                    name = entry[:-6].replace('/', '.')
                    references, strings = parse_class(data)
                    for reference in references:
                        if reference in self.module_of and reference != name:
                            self._add_edge(name, reference)
                    for string in strings:
                        for node in self.names_in_string(string):
                            if node != name:
                                self._add_edge(name, node)
                    if '$' in name:
                        # A nested class needs the file it's declared in
                        self._add_edge(name, name.split('$', 1)[0])
                elif entry == 'Resources/Info.plist':
                    # NSBundle loads a framework's principal class when it loads the framework
                    principal = PRINCIPAL_CLASS.search(read_text(data))
                    if principal and principal.group(1) in self.module_of:
                        self._add_edge(framework, principal.group(1))
                elif is_template(entry) and '.wo/' in entry:
                    component = entry.split('.wo/', 1)[0].rsplit('/', 1)[-1]
                    node = self.component_node(component)
                    for element in elements_in_template(read_text(data)):
                        for target in self.resolve(element):
                            if target != node:
                                self._add_edge(node, target)
                elif is_text_resource(entry):
                    # Framework resources, such as models, load with the framework. In its
                    # Properties, only values count: keys are defaults for every feature.
                    text = read_text(data)
                    if is_properties(entry):
                        text = properties_text(text, values_only=True)
                    for node in self.names_in_text(text):
                        self._add_edge(framework, node)
            for dependency in self.modules[module]:
                self._add_edge(framework, self.framework_node(dependency))
        for name, module in self.module_of.items():
            top = name.split('$', 1)[0]
            path = os.path.join(root, module, 'src', 'main', 'java', *top.split('.')) + '.java'
            self.source_file[name] = path if os.path.isfile(path) else None
        for path in set(p for p in self.source_file.values() if p):
            with open(path, 'rb') as f:
                self.lines[path] = f.read().count(b'\n')

    def _read_modules(self):
        with open(os.path.join(self.root, 'pom.xml')) as f:
            pom = f.read()
        version = re.search(r'<artifactId>wonder-lite</artifactId>\s*<version>([^<]+)</version>', pom).group(1)
        modules = {}
        for module in re.findall(r'<module>([^<]+)</module>', pom):
            if os.path.isfile(os.path.join(self.root, module, 'target', '%s-%s.jar' % (module, version))):
                modules[module] = set()
        if not modules:
            sys.exit('No %s jars under %s: build wonder-lite first.' % (version, self.root))
        for module in modules:
            with open(os.path.join(self.root, module, 'pom.xml')) as f:
                module_pom = f.read()
            for dependency in re.findall(r'<dependency>(.*?)</dependency>', module_pom, re.S):
                artifact = re.search(r'<artifactId>([^<]+)</artifactId>', dependency)
                if artifact and artifact.group(1) in modules:
                    modules[module].add(artifact.group(1))
        return version, modules

    def _add_edge(self, source, target):
        self.edges[source].add(target)

    @staticmethod
    def framework_node(module):
        return '<framework %s>' % module

    def component_node(self, name):
        """The class a component name stands for, or a node for a component that has none."""
        if name not in self.components:
            classes = self.by_simple_name.get(name)
            self.components[name] = sorted(classes)[0] if classes else '<component %s>' % name
        return self.components[name]

    def resolve(self, name):
        """The wonder-lite classes or components a name could stand for."""
        if '.' in name:
            return self.qualified(name)
        if name in self.by_simple_name:
            return self.by_simple_name[name]
        if name in self.components:
            return {self.components[name]}
        return set()

    def qualified(self, name):
        """The class a qualified name stands for, if any: er.x.Outer.Inner is er.x.Outer$Inner."""
        parts = name.split('.')
        first = next((i for i, part in enumerate(parts) if part[:1].isupper()), None)
        if not first:
            return set()
        package = '.'.join(parts[:first])
        for end in range(len(parts), first, -1):
            candidate = package + '.' + '$'.join(parts[first:end])
            if candidate in self.module_of:
                return {candidate}
        return set()

    def names_in_text(self, text):
        found = set()
        for match in QUALIFIED_NAME.finditer(text):
            found |= self.qualified(match.group(1))
        return found

    def names_in_string(self, string):
        """Classes a string constant names: by qualified name, or a component by its name, as
        in pageWithName(). Other simple names in strings are too often just words."""
        if QUALIFIED_NAME.fullmatch(string):
            return self.qualified(string)
        if string in self.components:
            return {self.components[string]}
        return set()


def elements_in_template(text):
    return set(WOD_ELEMENT.findall(text)) | set(INLINE_ELEMENT.findall(text))


class Roots:
    """What the apps name, and where they name it."""

    def __init__(self, wonder):
        self.wonder = wonder
        self.where = {}
        # wonder-lite classes that apps get from other jars instead
        self.shadowed = defaultdict(set)

    def add(self, nodes, where):
        for node in nodes:
            self.where.setdefault(node, where)

    def scan_app(self, app):
        for directory, subdirectories, files in os.walk(app):
            subdirectories[:] = sorted(d for d in subdirectories if d not in SKIPPED_DIRECTORIES and not d.startswith('.'))
            for file in sorted(files):
                path = os.path.join(directory, file)
                if file.endswith('.java'):
                    with open(path, 'rb') as f:
                        self.scan_java(read_text(f.read()), path)
                elif is_template(file) or is_text_resource(file):
                    with open(path, 'rb') as f:
                        self.scan_resource(file, read_text(f.read()), path)

    def scan_java(self, text, where):
        wonder = self.wonder
        self.add(wonder.names_in_text(text), where)
        packages = JAVA_ON_DEMAND_IMPORT.findall(text)
        if packages:
            for identifier in set(JAVA_IDENTIFIER.findall(text)):
                for package in packages:
                    self.add(wonder.qualified(package + '.' + identifier), where)
        for string in JAVA_STRING.findall(text):
            self.add(wonder.names_in_string(string), where)

    def scan_resource(self, name, text, where):
        wonder = self.wonder
        if is_template(name):
            for element in elements_in_template(text):
                self.add(wonder.resolve(element), where)
        elif is_properties(name):
            text = properties_text(text, values_only=False)
        self.add(wonder.names_in_text(text), where)

    def scan_pom(self, app):
        """The wonder-lite frameworks an app's POM declares, for when no classpath says."""
        path = os.path.join(app, 'pom.xml')
        if os.path.isfile(path):
            with open(path) as f:
                pom = f.read()
            for dependency in re.findall(r'<dependency>(.*?)</dependency>', pom, re.S):
                # The first artifactId is the dependency's, and any others are exclusions
                artifact = re.search(r'<artifactId>([^<]+)</artifactId>', dependency)
                if artifact and artifact.group(1) in self.wonder.modules:
                    self.add({self.wonder.framework_node(artifact.group(1))}, path)

    def scan_classpath(self, path):
        """The wonder-lite frameworks on an app's classpath, and what its other frameworks name."""
        with open(path) as f:
            entries = [e for e in f.read().strip().split(os.pathsep) if e]
        modules = {module_of_path(entry, self.wonder.modules) for entry in entries} - {None}
        for entry in entries:
            module = module_of_path(entry, self.wonder.modules)
            if module:
                self.add({self.wonder.framework_node(module)}, entry)
            elif entry.endswith('.jar') and os.path.isfile(entry):
                with zipfile.ZipFile(entry) as jar:
                    names = jar.namelist()
                # A class wonder-lite also has comes from this jar only if the app lacks the
                # wonder-lite framework. Otherwise it's one of Wonder's patches, which wins.
                for name in names:
                    binary = name[:-6].replace('/', '.')
                    if name.endswith('.class') and binary in self.wonder.module_of and self.wonder.module_of[binary] not in modules:
                        self.shadowed[os.path.basename(entry)].add(binary)
                if 'Resources/Info.plist' in names:
                    self.scan_jar(entry)

    def nodes(self):
        """The roots, less classes that apps get from other jars."""
        shadowed = set().union(*self.shadowed.values()) if self.shadowed else set()
        return {node: where for node, where in self.where.items() if node not in shadowed}

    def scan_jar(self, path):
        """What the classes and resources in a jar, or a directory of them, name."""
        if os.path.isdir(path):
            entries = []
            for directory, _, files in os.walk(path):
                for file in files:
                    full = os.path.join(directory, file)
                    entries.append((os.path.relpath(full, path).replace(os.sep, '/'), full))
            read = read_file
        else:
            jar = zipfile.ZipFile(path)
            entries = [(name, name) for name in jar.namelist()]
            read = jar.read
        wonder = self.wonder
        for name, key in entries:
            where = '%s!%s' % (os.path.basename(path), name)
            if name.endswith('.class'):
                references, strings = parse_class(read(key))
                self.add({r for r in references if r in wonder.module_of}, where)
                for string in strings:
                    self.add(wonder.names_in_string(string), where)
            elif is_template(name) or is_text_resource(name):
                self.scan_resource(name, read_text(read(key)), where)


def module_of_path(path, modules):
    """The wonder-lite (or Wonder) framework a jar or directory path belongs to, if any."""
    jar = re.match(r'([A-Za-z]+)-\d+(?:\.\d+)*(?:-SNAPSHOT|-\d{8}\.\d{6}-\d+)?\.jar$', os.path.basename(path))
    if jar and jar.group(1) in modules:
        return jar.group(1)
    for pattern in (r'/([A-Za-z]+)\.framework/', r'/([A-Za-z]+)/(?:bin|target/classes)/'):
        directory = re.search(pattern, path)
        if directory and directory.group(1) in modules:
            return directory.group(1)
    return None


LOG_LINE = re.compile(r'(?:\[class,load\] (\S+) source: |\[Loaded (\S+) from )(?:jar:)?(\S+?)\]?$')


def loaded_classes(logs, wonder):
    """The wonder-lite classes the logs show loaded, from -Xlog:class+load or -verbose:class."""
    loaded = set()
    for log in logs:
        with open(log, errors='replace') as f:
            for line in f:
                match = LOG_LINE.search(line.rstrip())
                if not match:
                    continue
                name = match.group(1) or match.group(2)
                if name in wonder.module_of and module_of_path(match.group(3), wonder.modules) == wonder.module_of[name]:
                    loaded.add(name)
    return loaded


def reach(wonder, roots):
    """Every node reachable from the roots, with the node it was first reached from."""
    nodes = roots.nodes()
    parent = {node: None for node in nodes}
    queue = deque(sorted(nodes))
    while queue:
        node = queue.popleft()
        for target in sorted(wonder.edges.get(node, ())):
            if target not in parent:
                parent[target] = node
                queue.append(target)
    return parent


def chain(node, parent, wonder):
    """How a node was reached, from its root."""
    steps = []
    while node is not None:
        steps.append(node.rsplit('.', 1)[-1] if node in wonder.module_of else node)
        node = parent[node]
    return ' < '.join(steps)


def report(wonder, roots, parent, loaded, details):
    files = defaultdict(set)
    for name, module in wonder.module_of.items():
        files[(module, wonder.source_file[name])].add(name)
    totals = defaultdict(lambda: defaultdict(int))
    rows = []
    for (module, path), names in sorted(files.items(), key=lambda item: (item[0][0], item[0][1] or '')):
        if path is None:
            continue
        lines = wonder.lines[path]
        reached = sorted(n for n in names if n in parent)
        static = bool(reached)
        runtime = any(n in loaded for n in names)
        kinds = {'all': True, 'static': static, 'loaded': runtime, 'either': static or runtime, 'neither': not (static or runtime)}
        for kind, counted in kinds.items():
            if counted:
                totals[module][kind] += lines
        if reached:
            first = min(reached, key=lambda n: len(chain(n, parent, wonder)))
            root = first
            while parent[root] is not None:
                root = parent[root]
            how = chain(first, parent, wonder), roots.where[root]
        else:
            how = '', ''
        rows.append((module, os.path.relpath(path, os.path.join(wonder.root, module, 'src', 'main', 'java')), lines, static, runtime) + how)
    print('%-22s %15s %15s %15s %15s %15s' % ('framework', 'lines', 'static', 'loaded', 'either', 'neither'))
    grand = defaultdict(int)
    for module in sorted(totals):
        counts = totals[module]
        for key, value in counts.items():
            grand[key] += value
        print_row(module, counts)
    print_row('total', grand)
    print()
    print('Lines of Java in the source files that the apps reach statically (static), that the')
    print('logs show loaded (loaded), either or neither, each also as a share of all the lines.')
    print('Files that neither covers are candidates for deletion.')
    for jar, names in sorted(roots.shadowed.items()):
        print('%s provides %d wonder-lite classes itself, so they don\'t count: %s' % (jar, len(names), ', '.join(sorted(n.rsplit('.', 1)[-1] for n in names))))
    if details:
        with open(details, 'w') as f:
            f.write('framework\tfile\tlines\tstatic\tloaded\treached through\tnamed in\n')
            for module, path, lines, static, runtime, how, where in rows:
                f.write('%s\t%s\t%d\t%s\t%s\t%s\t%s\n' % (module, path, lines, 'yes' if static else 'no', 'yes' if runtime else 'no', how, where))


def print_row(name, counts):
    total = counts['all'] or 1
    cells = ['%7d %3d%%' % (counts[kind], 100 * counts[kind] // total) for kind in ('static', 'loaded', 'either', 'neither')]
    print('%-22s %15d %s' % (name, counts['all'], ' '.join('%15s' % cell for cell in cells)))


def main():
    parser = argparse.ArgumentParser(description=__doc__.split('\n\n')[0], epilog='See README.md.')
    parser.add_argument('apps', metavar='APP', nargs='+', help='an app checkout to scan')
    parser.add_argument('--wonder-lite', metavar='DIR', default=os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'), help='a built wonder-lite checkout (default: this one)')
    parser.add_argument('--classpath', metavar='FILE', action='append', default=[], help="an app's runtime classpath, from mvn dependency:build-classpath")
    parser.add_argument('--jar', metavar='PATH', action='append', default=[], help='a jar or classes directory to scan as well, such as a framework not on a --classpath')
    parser.add_argument('--log', metavar='FILE', action='append', default=[], help='a class-load log')
    parser.add_argument('--details', metavar='FILE', help='write a line per source file, with how it was reached, to FILE')
    args = parser.parse_args()
    wonder = WonderLite(os.path.abspath(args.wonder_lite))
    roots = Roots(wonder)
    for app in args.apps:
        roots.scan_app(os.path.abspath(app))
        if not args.classpath:
            roots.scan_pom(os.path.abspath(app))
    for classpath in args.classpath:
        roots.scan_classpath(classpath)
    for jar in args.jar:
        roots.scan_jar(jar)
    report(wonder, roots, reach(wonder, roots), loaded_classes(args.log, wonder), args.details)


if __name__ == '__main__':
    main()
