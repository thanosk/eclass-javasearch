#!/bin/bash

javac -d bin \
-cp lib/mysql-connector-java-5.1.22-bin.jar \
-cp lib/lucene-core-3.6.1.jar \
./src/java/org/openeclass/lucene/demo/IndexCourses.java \
./src/java/org/openeclass/lucene/demo/PropertyLoader.java

cp src/java/project-properties.xml bin
