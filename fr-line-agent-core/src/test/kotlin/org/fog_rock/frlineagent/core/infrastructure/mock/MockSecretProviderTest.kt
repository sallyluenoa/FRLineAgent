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

package org.fog_rock.frlineagent.core.infrastructure.mock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MockSecretProviderTest {

    @Test
    fun testGetSecret_success() {
        val secrets = mapOf(
            "LINE_CHANNEL_ACCESS_TOKEN" to "custom_line_channel_access_token",
            "CUSTOM_KEY" to "custom_value"
        )
        val secretProvider = MockSecretProvider(secrets)

        assertEquals("custom_line_channel_access_token", secretProvider.getSecret("LINE_CHANNEL_ACCESS_TOKEN"))
        assertEquals("custom_value", secretProvider.getSecret("CUSTOM_KEY"))
    }

    @Test
    fun testGetSecret_failure() {
        // Test with empty secrets.
        val emptySecretProvider = MockSecretProvider()
        assertThrows<IllegalArgumentException> {
            emptySecretProvider.getSecret("UNKNOWN_KEY")
        }

        // Test with non-empty secrets.
        val secrets = mapOf("EXISTING_KEY" to "EXISTING_VALUE")
        val secretProvider = MockSecretProvider(secrets)
        assertThrows<IllegalArgumentException> {
            secretProvider.getSecret("UNKNOWN_KEY")
        }
    }
}
