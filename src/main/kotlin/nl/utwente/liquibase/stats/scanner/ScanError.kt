package nl.utwente.liquibase.stats.scanner

sealed class ScanError {
    abstract val path: String
    data class InvalidFile(override val path: String) : ScanError()
    data class ParsingError(override val path: String, val msg: String) : ScanError()
    data class NoMasterFile(override val path: String) : ScanError()
}
