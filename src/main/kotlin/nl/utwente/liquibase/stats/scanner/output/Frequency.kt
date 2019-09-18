package nl.utwente.liquibase.stats.scanner.output

data class Frequency(val total: Int, val rollbackCount: Int) {
    companion object {
        fun empty() = Frequency(0, 0)
    }
}
