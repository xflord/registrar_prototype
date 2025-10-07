This is a test implementation of a pluggable module. The module itself can be extracted into a separate repository (
as long as the `extension-api` package is available as dependency (e.g. gitlab repository)).

The module then has to be built into a jar file and placed in the `plugins` directory (defined in core app).
