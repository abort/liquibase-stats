# Liquibase Statistics App

This tool gathers statistics of change patterns for Liquibase based projects. Liquibase is a schema version management tool used store database changes as code.

## How to build
run `mvn install` to build a jar. 

## How to run
run `java -jar target/liquibase-statistics-1.0-SNAPSHOT.jar --path "$HOME/projects/project1/src/main/resources" --path "$HOME/projects/project2/src/main/resources"` to collect statistics over 2 projects. Recursive references to Liquibase changelog files that contain relative paths, are resolved using the provided path as a base path. E.g. a `db.changelog-master.yaml` that includes `db/changelog/v1.yml`, will resolve to `$HOME/projects/project1/src/main/resources/db/changelog/v1.yml`.

# Analysis in Thesis
This section contains data and details of the analysis in the thesis.

## Latest statistics 
| | addColumn | addNotNullConstraint | addPrimaryKey | createIndex | createSequence | createTable | dropColumn | dropIndex | dropSequence | dropTable | modifyDataType | renameColumn | renameTable | sql | sqlFile | tagDatabase | update |N<sub>ChangeSets</sub> | N<sub>RollbackableChangeSets</sub> | N<sub>Changes</sub> | N<sub>RollbackableChanges</sub> |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---|
<b>aax</b> | 6 | 0 | 0 | 8 | 4 | 2 | 4 | 0 | 2 | 1 | 0 | 3 | 0 | 1 | 0 | 0 | 0 | 10 | 7 | 31 | 23
<b>pam</b> | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 5392 | 739 | 0 | 6131 | 1545 | 6131 | 739
<b>cha</b> | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 3437 | 646 | 0 | 4083 | 1353 | 4083 | 646
<b>irs</b> | 10 | 4 | 3 | 6 | 0 | 5 | 1 | 2 | 0 | 1 | 1 | 5 | 1 | 8 | 1 | 0 | 2 | 38 | 24 | 50 | 34
<b>srv</b> | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 5 | 2 | 0 | 7 | 4 | 7 | 2
<b>cpa</b> | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1511 | 60 | 0 | 1571 | 111 | 1571 | 60
<b>nfe</b> | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 478 | 109 | 3 | 572 | 267 | 590 | 109
|||||||||||||||||||12412 | 9335 | 12463 | 1613 |
ChangeSets with SQL that have no custom rollback: 10815

Date of analysis: 27th of August 2020

## Observations on PAM
Below table entails a manual analysis of schema changes to the Party and Agreement Management application. The data examined spans over more than 2 years starting from version 3.0.0. The data is from December 2018 up to 9th of March 2021. The data has been categorized in Liquibase (Community Edition) native constructs rather than plain SQL to keep it comparable to the above table.

| addColumn | addForeignKey | addPrimaryKey | addUniqueConstraint | createIndex | createSequence | createTable | dropColumn | dropForeignKey | dropIndex | dropNotNullConstraint | dropTable | dropUniqueConstraint | modifyDataType
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---
| 79\* | 38 | 34 | 32 | 15 | 37 | 37 | 3 | 3 | 2 | 10 | 4 | 7 | 2\*\*

\* of which 2 are Non-Nullable, whereas the rest have Default or Nullable

\*\*: both to extend the current data type (`varchar2(35)` to `varchar2(70)`)

\*\*\*: PAM only contains triggers to select the next value of a sequence
