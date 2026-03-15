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

package org.fog_rock.frlineagent.infrastructure.internal.cloud

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.slf4j.LoggerFactory

internal class GoogleSecretProvider(private val projectNumber: String) : SecretProvider {
    private val logger = LoggerFactory.getLogger(GoogleSecretProvider::class.java)

    override fun getSecret(key: String): String {
        if (projectNumber.isBlank()) {
            throw IllegalStateException("Google Cloud Project Number is not set.")
        }

        logger.info("projectNumber: $projectNumber")
        logger.info("key: $key")

        val trimmedProjectNumber = projectNumber.trim()
        val trimmedKey = key.trim()

        logger.info("trimmedProjectNumber: $trimmedProjectNumber")
        logger.info("trimmedKey: $trimmedKey")

        val debugPath = "projects/$trimmedProjectNumber/secrets/$trimmedKey/versions/latest"
        logger.info("DEBUG: Accessing SecretManager with path: [$debugPath]")

        SecretManagerServiceClient.create().use { client ->
            val secretVersionName = SecretVersionName.of(trimmedProjectNumber, trimmedKey, "latest")
            val response = client.accessSecretVersion(secretVersionName)
            return response.payload.data.toStringUtf8()
        }
    }
}
