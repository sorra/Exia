Exia
====

A simple, light framework for mass code analysis and manipulation, proved in 2 million lines of Java 7 code.

----

How to build: ant or eclipse build
command: 
  >ant

How to run: java -jar exia-mt.jar [case-name] [paths...]
example: 
  >java -jar exia-mt.jar unused-import-delete /home/sorra/projects/

----

To learn how to use it, please see src/github/exia/samples/.

There are examples showing you:
>1. Detect the places you have forgotten to write "logger.isDebugEnabled()"
>2. Fix a type of misuse of logger.error() API
>3. Remove unused imports

----

There are two extension points: FileFilter & AstFunction.
Implement them and call FileWalker to run your own operations.  
(github.exia.provided.JavaSourceFileFilter is ready to use, filtering *.java files)

github.exia.ast.util provides convenient utilities.
