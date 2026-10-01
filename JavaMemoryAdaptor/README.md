JavaMemoryAdaptor
=================
An EOF adaptor that keeps rows in memory, for tests. Models that set
their adaptor name to `Memory` use it. WOUnit's editing-context rules
depend on it.

Adaptor lookup
--------------
EOF finds an adaptor's class through the `EOAdaptorClassName` key in its
framework's `Info.plist`. WOProject's framework template ignores the
`eoAdaptorClassName` setting in `build.properties`, so the key comes
from `customInfoPListContent` there instead. Keep both settings: WOLips
reads the first, and the Maven build needs the second.

Wonder's own JavaMemoryAdaptor builds lacked the key, which is why
WOUnit patches it in at runtime.
