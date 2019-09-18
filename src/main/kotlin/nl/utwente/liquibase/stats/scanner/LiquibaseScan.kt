package nl.utwente.liquibase.stats.scanner

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.firstOrNone
import arrow.core.flatMap
import arrow.data.Validated
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.exception.ChangeLogParseException
import liquibase.parser.ChangeLogParserFactory
import liquibase.resource.FileSystemResourceAccessor
import java.io.File

class LiquibaseScan(private val paths: Collection<String>) {
    companion object {
        val Extensions = setOf("yml", "yaml", "xml")
    }

    private val parserFactory = ChangeLogParserFactory.getInstance()
    private val parsers =
        paths.associate { root ->
            Pair(File(root), extMap(root))
        }

    private fun extMap(root: String) =
        Extensions.map { ext -> Pair(ext, parserFactory.getParser(".$ext", FileSystemResourceAccessor(root))) }.toMap()

    private fun collectFiles(p: File, extension: Option<String>): Set<File> {
        assert(p.isDirectory)
        val files = p.walkTopDown().toSet()
        return extension.map { ext -> files.filter { it.extension == ext } }.getOrElse { files }.toSet()
    }

    fun parse(root: File, p: File): Validated<ScanError, DatabaseChangeLog> {
        assert(p.extension in Extensions)

        return try {
            val log = parsers[root]?.get(p.extension)
                ?.parse(p.absolutePath, ChangeLogParameters(), FileSystemResourceAccessor(root.absolutePath))

            if (log == null) {
                Validated.Invalid(ScanError.InvalidFile(p.absolutePath))
            } else {
                Validated.Valid(log)
            }
        } catch (e: ChangeLogParseException) {
            println(e.cause)
            Validated.Invalid(ScanError.ParsingError(p.absolutePath, e.message!!))
        }
    }

    fun parse(extension: Option<String>): Set<Validated<ScanError, DatabaseChangeLog>> = paths.flatMap { root ->
        val (rootFile, files) = walk(root, extension)
        files.map { parse(rootFile, it) }
    }.toSet()

    private fun walk(
        root: String,
        extension: Option<String>
    ): Pair<File, Set<File>> {
        val rootFile = File(root)
        val files = collectFiles(rootFile, extension)
        return Pair(rootFile, files)
    }

    fun parseMasterFiles(): Collection<Either<ScanError, DatabaseChangeLog>> = paths.map { root ->
        val (rootFile, files) = walk(root, None)
        files.firstOrNone { f -> f.nameWithoutExtension.endsWith("changelog-master") }.toEither {
            ScanError.NoMasterFile(
                root
            )
        }.flatMap {
            parse(rootFile, it).toEither()
        }
    }
}
