#!/bin/bash


java -cp bin:lib/lucene-core-3.6.1.jar:lib/mysql-connector-java-5.1.22-bin.jar \
org.openeclass.lucene.demo.IndexCourses $1
