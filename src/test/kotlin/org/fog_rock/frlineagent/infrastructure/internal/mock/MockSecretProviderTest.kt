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

package org.fog_rock.frlineagent.infrastructure.internal.mock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MockSecretProviderTest {

    private val secretProvider = MockSecretProvider()

    @Test
    fun testGetSecret_success() {
        assertEquals("mock_line_channel_access_token", secretProvider.getSecret("LINE_CHANNEL_ACCESS_TOKEN"))
        assertEquals("mock_line_channel_secret", secretProvider.getSecret("LINE_CHANNEL_SECRET"))
        assertEquals("mock_spreadsheet_id", secretProvider.getSecret("SPREADSHEET_ID"))
        assertEquals("{}", secretProvider.getSecret("GOOGLE_CREDENTIALS_JSON"))
    }

    @Test
    fun testGetSecret_failure() {
        assertThrows<IllegalArgumentException> {
            secretProvider.getSecret("UNKNOWN_KEY")
        }
    }
}
