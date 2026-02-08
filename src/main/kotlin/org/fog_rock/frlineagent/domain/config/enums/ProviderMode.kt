/*
 * Copyright (c) 2026 SallyLueNoa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fog_rock.frlineagent.domain.config.enums

/**
 * Provider mode.
 *
 * @property value The string value of the mode.
 */
enum class ProviderMode(val value: String) {
    /** Use cloud services. */
    CLOUD("cloud"),
    /** Use mocks. */
    MOCK("mock"),
    ;

    companion object {
        /**
         * Converts a string value to a ProviderMode.
         *
         * @param value The string value to convert.
         * @return The corresponding ProviderMode.
         * @throws IllegalArgumentException If the value is not found in the enum.
         */
        fun convert(value: String?): ProviderMode = entries.find { it.value.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Value `$value` not found in enum.")
    }
}
