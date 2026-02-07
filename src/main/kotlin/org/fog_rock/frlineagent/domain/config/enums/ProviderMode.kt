package org.fog_rock.frlineagent.domain.config.enums

/**
 * Provider mode.
 */
enum class ProviderMode(val value: String) {
    /** Use cloud services. */
    CLOUD("cloud"),
    /** Use mocks. */
    MOCK("mock"),
    ;

    companion object {
        fun convert(value: String?): ProviderMode = entries.find { it.value.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Value `$value` not found in enum.")
    }
}
