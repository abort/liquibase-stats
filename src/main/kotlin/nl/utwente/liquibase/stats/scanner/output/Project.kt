package nl.utwente.liquibase.stats.scanner.output

import java.util.SortedMap

data class Project(
    val name: String,
    val changeSetFrequency: Frequency,
    val changesCount: Int,
    val changeFrequencies: SortedMap<String, Frequency>
)
