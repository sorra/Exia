Exia
====

Proved on 2 million lines of code: a tooling framework for automatic analysis and modification on large codebases.

Supports Java 6/7/8 (Modification on Java 8 code is not guaranteed).

### Usage

Install JDK 8 before using.

Build: `gradle shadowJar`

Run: `java -jar build/libs/exia-2.0-all.jar [class-name] [project-paths...]`

For example: `java -jar build/libs/exia-2.0-all.jar UnusedImportDeletor /home/sorra/projects/`

----

To learn how to write your own program, please see the package `com.iostate.exia.samples`.

There are samples showing you:
1. Detect the places you have forgotten to write `logger.isDebugEnabled()`
2. Fix a type of misuse of `logger.error()` API
3. Remove unused imports

----

There are two extension points: `FileFilter` & `AstFunction`.
Implement them and call `FileWalker` to run your own operations.  
(The default FileFilter is `com.iostate.exia.api.JavaSourceFileFilter`)

The package `com.iostate.exia.ast` provides convenient AST utilities.

### What's new in 2.0?

- Implemented in Java 8.
- Upgraded JDT library to 3.12.3 in maven central.
- Changed the build tool to Gradle.
- Better API.
- More useful samples.
- Will add intelligence.
