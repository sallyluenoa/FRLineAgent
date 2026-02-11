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

import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.config.ProviderMode
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.fog_rock.frlineagent.infrastructure.internal.cloud.GoogleSecretProvider
import org.fog_rock.frlineagent.infrastructure.internal.mock.MockSecretProvider

class SecretManagerProvider(appConfig: AppConfig) : SecretProvider {

    private val provider: SecretProvider = when (appConfig.secretManagerMode) {
        ProviderMode.CLOUD -> GoogleSecretProvider(appConfig.googleCloudProjectId)
        ProviderMode.MOCK -> MockSecretProvider()
    }

    override fun getSecret(key: String): String = provider.getSecret(key)
}
