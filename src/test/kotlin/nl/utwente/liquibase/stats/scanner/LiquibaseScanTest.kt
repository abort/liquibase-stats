package nl.utwente.liquibase.stats.scanner

import arrow.core.some
import arrow.data.Validated
import liquibase.change.core.CreateSequenceChange
import liquibase.change.core.CreateTableChange
import liquibase.changelog.DatabaseChangeLog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class LiquibaseScanTest {

    @Test
    fun ymlLiquibaseScriptsGetParsed() {
        val r = javaClass.getResource("/lb/yml")!!
        val file = r.path
        val scan = LiquibaseScan(setOf(file))
        val changelogs = scan.parse("yml".some())
        assertEquals(1, changelogs.size)
        val change = changelogs.first()

        assertFalse(change.isInvalid)

        assertEquals(
            1,
            when (change) {
                is Validated.Valid<DatabaseChangeLog> -> change.a.changeSets.size
                else -> -1
            }
        )
        when (change) {
            is Validated.Valid<DatabaseChangeLog> -> {
                val c = change.a
                assertEquals(1, c.changeSets.size)

                val changeSet = c.changeSets.first()
                with(changeSet) {
                    assertEquals("Jorryt", author)
                    assertEquals("Initial schema", comments)
                    assertEquals(false, isAlwaysRun)
                    assertEquals(0, preconditions?.nestedPreconditions?.size ?: 0)
                    assertEquals(4, changes.size)
                    assertTrue(changes[0] is CreateTableChange)
                    assertTrue(changes[1] is CreateTableChange)
                    assertTrue(changes[2] is CreateSequenceChange)
                    assertTrue(changes[3] is CreateSequenceChange)
                }
            }
            else -> {

            }
        }
    }

    @Test
    fun xmlLiquibaseScriptsGetParsed() {
        val r = javaClass.getResource("/lb/xml")!!
        val file = r.path
        val scan = LiquibaseScan(setOf(file))
        val changelogs = scan.parse("xml".some())
        assertEquals(2, changelogs.size)
        val change = changelogs.first()

        assertFalse(change.isInvalid)

        assertEquals(
            1,
            when (change) {
                is Validated.Valid<DatabaseChangeLog> -> change.a.changeSets.size
                else -> -1
            }
        )
    }
}
