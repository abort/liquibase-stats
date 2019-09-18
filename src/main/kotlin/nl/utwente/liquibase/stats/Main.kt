package nl.utwente.liquibase.stats

import arrow.core.Either
import arrow.core.getOrElse
import arrow.data.extensions.list.foldable.exists
import arrow.data.extensions.list.foldable.forAll
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import nl.utwente.liquibase.stats.scanner.LiquibaseScan
import nl.utwente.liquibase.stats.scanner.ScanError
import nl.utwente.liquibase.stats.scanner.output.Frequency
import nl.utwente.liquibase.stats.scanner.output.Project
import liquibase.change.Change
import liquibase.change.core.EmptyChange
import liquibase.change.core.RawSQLChange
import liquibase.change.core.SQLFileChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.OracleDatabase
import java.io.File

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand() {
    private val path: List<String> by option(help = "Path to master file").multiple(required = true)
    // For now only support oracle
    private val database = OracleDatabase()

    override fun run() {
        val scanner = LiquibaseScan(path)
        val names = path.map { p ->
            val x = if (p.last() == File.separatorChar) p.dropLast(1) else p
            x.takeLastWhile { it != File.separatorChar }
        }
        val results = names.zip(scanner.parseMasterFiles()).toMap()

        // Logs variable is only used to collect all keys
        val logs = results.values.fold(emptyList<DatabaseChangeLog>()) { acc, v ->
            when (v) {
                is Either.Right<DatabaseChangeLog> -> acc + v.b
                else -> acc
            }
        }

        printMarkDown(logs, results)

        // Display each log
        // displayLogStructure(logs)

        println("Scanned ${results.size} master files")
    }

    // Count changesets as rollbackable which:
    // 1. have all changes support current database for rollback; OR
    // 2. has a rollback that supports the current database
    private fun ChangeSet.isRollbackable(): Boolean =
        changes.forAll { c -> c.supportsRollback(database) } ||
            (
                (
                    rollback.changes.exists { c ->
                        val cls = c::class
                        cls != EmptyChange::class
                    }
                    ) && supportsRollback(database)
                )

    // && supportsRollback(Database)
    private fun printMarkDown(
        logs: List<DatabaseChangeLog>,
        results: Map<String, Either<ScanError, DatabaseChangeLog>>
    ) {
        val keys = logs.flatMap { collectChanges(it).map { c -> c.toString() } }.distinct()
        fun toFrequency(changeGroup: Map.Entry<String, List<Change>>) =
            Frequency(
                changeGroup.value.size,
                changeGroup.value.count { c -> c.supportsRollback(database) }
            )

        val projects = results.map { (name, r) ->
            when (r) {
                is Either.Right<DatabaseChangeLog> -> {
                    val dbc = r.b
                    // Collect all changes and their frequency
                    val changeMap = keys.map { k -> Pair(k, 0) }.toMap()

                    // Collect amount of changesets and compute amount of changesets with rollback
                    val customRollbacksPerChangeSetFrequency =
                        Frequency(
                            dbc.changeSets.size,
                            dbc.changeSets.count {
                                it.isRollbackable()
                            }
                        )
                    val changes = collectChanges(dbc)
                    val counts = changes.groupBy { it.toString() }.mapValues { toFrequency(it) }
                        .toMutableMap()
                    changeMap.forEach { (k, _) -> counts.putIfAbsent(k, Frequency.empty()) }
                    Project(name, customRollbacksPerChangeSetFrequency, changes.size, counts.toSortedMap())
                }
                is Either.Left<ScanError> -> {
                    Project(
                        name,
                        Frequency(0, 0),
                        0,
                        keys.map { k -> Pair(k, Frequency.empty()) }.toMap().toSortedMap()
                    )
                }
            }
        }

        val allChangeSets = results.values.flatMap { v ->
            v.map { log ->
                log.changeSets
            }.getOrElse {
                emptyList<ChangeSet>()
            }
        }

        File("freq.md").bufferedWriter().use { out ->
            // Print heading
            val changeTypes = projects.flatMap { it.changeFrequencies.keys }.toSortedSet()
            out.write(changeTypes.joinToString(prefix = "| | ", separator = " | ", postfix = " |"))
            out.appendln("N<sub>ChangeSets</sub> | N<sub>RollbackableChangeSets</sub> | N<sub>Changes</sub> | N<sub>RollbackableChanges</sub> |")
            val n = changeTypes.size + 5
            out.appendln(
                IntRange(1, n).joinToString(prefix = "| ", separator = " | ", postfix = "|") { ":---" }
            )

            // Print details
            projects.forEach { (project, rollbackFreq, changes, changeFrequencies) ->
                val freqs = changeTypes.map { changeFrequencies[it]!!.total }
                val items =
                    listOf("<b>$project</b>") + freqs + rollbackFreq.total + rollbackFreq.rollbackCount + changes + changeFrequencies.values.sumBy { it.rollbackCount }
                out.appendln(items.joinToString(separator = " | "))
            }

            // Print row with total values
            out.append(
                IntRange(1, n - 3).joinToString(separator = "") { "|" }
            )
            out.appendln(
                listOf(
                    allChangeSets.size,
                    allChangeSets.count { !it.rollback.changes.isNullOrEmpty() && it.supportsRollback(OracleDatabase()) },
                    projects.sumBy { it.changesCount },
                    projects.sumBy { it.changeFrequencies.values.sumBy { f -> f.rollbackCount } }
                ).joinToString(separator = " | ", postfix = " |")
            )

            val rollbackCount = allChangeSets.count { set ->
                val c = set.changes.map { it::class }
                c.contains(SQLFileChange::class) || c.contains(RawSQLChange::class)
            }

            // Print addendum
            out.appendln("ChangeSets with SQL that have no custom rollback: $rollbackCount")
        }
    }

    private fun collectChanges(log: DatabaseChangeLog) = log.changeSets.flatMap { it.changes }
}
