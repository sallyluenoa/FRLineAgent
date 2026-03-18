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

import org.fog_rock.frlineagent.domain.repository.SecretProvider

internal class MockSecretProvider : SecretProvider {
    private val secrets = mapOf(
        "LINE_CHANNEL_ACCESS_TOKEN" to "mock_line_channel_access_token",
        "LINE_CHANNEL_SECRET" to "mock_line_channel_secret",
        "SPREADSHEET_ID" to "mock_spreadsheet_id",
        "GOOGLE_CREDENTIALS_JSON" to "{}"
    )

    override fun getSecret(key: String): String =
        secrets[key] ?: throw IllegalArgumentException("Secret key `$key` not found in mock.")
}
