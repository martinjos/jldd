JLDD - Java Lister of Dynamic Dependencies
==========================================

Lists the dynamic dependencies of a JAR file among a set of JAR files.


Usage
-----

```sh
jldd jar-to-test.jar "jar1.jar;jar2.jar;..."
```

Prints an acyclical dependency tree of external references rooted at
`jar-to-test.jar`.  The jars are searched for dependencies in the following
order:

1. First, the root jar itself (in the first argument) is searched.

2. Then, the jars given in the second argument are searched, in the order given.

3. Finally, the current JRE's `rt.jar` is searched.

If the second argument is omitted, the current JRE's "classpath" is used (that
is, the JRE of the process running jldd).

This same search order is used even when recursing to a lower level of the
dependency tree.

Note that unlike the Linux program ldd(1), this will *never* result in code
execution.


License
-------

Copyright (c) 2015 Martin Sidaway

Permission (in the form of a perpetual, worldwide, non-exclusive, no-charge,
royalty-free, irrevocable copyright and patent license) is hereby granted, free
of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without
restriction, including without limitation (and in any combination) the rights
to use, copy, modify, merge, reimplement, publish, distribute, sublicense,
and/or sell copies of the Software (and/or portions thereof), and to permit
persons to whom the Software is furnished to do so.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT OR PATENT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
