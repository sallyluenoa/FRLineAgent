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

package org.fog_rock.frlineagent.infrastructure.external

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName

class SecretManagerProvider {

    fun getSecret(key: String): String {
        val env = System.getenv("APP_ENV") ?: "local"
        return if (env == "local") {
            System.getenv(key) ?: throw IllegalArgumentException("Environment variable $key not found.")
        } else {
            getSecretFromCloud(key)
        }
    }

    private fun getSecretFromCloud(secretId: String): String {
        val projectId = System.getenv("GOOGLE_CLOUD_PROJECT")
            ?: throw IllegalStateException("GOOGLE_CLOUD_PROJECT environment variable is not set.")

        SecretManagerServiceClient.create().use { client ->
            val secretVersionName = SecretVersionName.of(projectId, secretId, "latest")
            val response = client.accessSecretVersion(secretVersionName)
            return response.payload.data.toStringUtf8()
        }
    }
}
