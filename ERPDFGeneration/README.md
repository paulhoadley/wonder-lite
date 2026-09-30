ERPDFGeneration
===============
Generates PDF documents, either through the `ERPDFUtilities` class or
by wrapping page content in the `ERPDFWrapper` component.

It supports three engines:

* [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer),
  which renders XHTML and CSS.
* [UJAC](http://ujac.sourceforge.net/), with its own set of `UJAC*`
  components.
* [Apache FOP](https://xmlgraphics.apache.org/fop/), which renders
  XSL-FO through `ERFOPWrapper`.
