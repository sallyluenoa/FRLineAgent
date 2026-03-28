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

package org.fog_rock.frlineagent.core.plugin

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import org.fog_rock.frlineagent.core.domain.config.ProviderMode
import org.fog_rock.frlineagent.core.domain.repository.SecretProvider
import org.fog_rock.frlineagent.core.domain.service.LineClient
import org.fog_rock.frlineagent.core.domain.service.SignatureVerifier
import org.fog_rock.frlineagent.core.infrastructure.cloud.GoogleSecretProvider
import org.fog_rock.frlineagent.core.infrastructure.cloud.LineMessagingCloudClient
import org.fog_rock.frlineagent.core.infrastructure.cloud.LineSignatureCloudVerifier
import org.fog_rock.frlineagent.core.infrastructure.mock.LineMessagingMockClient
import org.fog_rock.frlineagent.core.infrastructure.mock.LineSignatureMockVerifier
import org.fog_rock.frlineagent.core.infrastructure.mock.MockSecretProvider
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * A Ktor plugin for FRLineAgent.
 * This plugin simplifies the integration of a LINE Bot into a Ktor application.
 */
val FRLineAgent = createApplicationPlugin(
    name = "FRLineAgent",
    createConfiguration = ::Configuration
) {
    val koinModule = module {
        single<SecretProvider> {
            when (pluginConfig.secretManagerMode) {
                ProviderMode.CLOUD -> {
                    val projectNumber = requireNotNull(pluginConfig.googleCloudProjectNumber) {
                        "googleCloudProjectNumber must be set when secretManagerMode is CLOUD."
                    }
                    GoogleSecretProvider(projectNumber)
                }
                ProviderMode.MOCK -> MockSecretProvider()
            }
        }
        single<LineClient> {
            when (pluginConfig.lineApiMode) {
                ProviderMode.CLOUD -> {
                    val accessTokenKey = requireNotNull(pluginConfig.lineBotChannelAccessTokenKey) {
                        "lineBotChannelAccessTokenKey must be set when lineApiMode is CLOUD."
                    }
                    LineMessagingCloudClient(get<SecretProvider>().getSecret(accessTokenKey))
                }
                ProviderMode.MOCK -> LineMessagingMockClient()
            }
        }
        single<SignatureVerifier> {
            when (pluginConfig.lineApiMode) {
                ProviderMode.CLOUD -> {
                    val secretKey = requireNotNull(pluginConfig.lineBotChannelSecretKey) {
                        "lineBotChannelSecretKey must be set when lineApiMode is CLOUD."
                    }
                    LineSignatureCloudVerifier(get<SecretProvider>().getSecret(secretKey))
                }
                ProviderMode.MOCK -> LineSignatureMockVerifier()
            }
        }
    }

    application.install(Koin) {
        slf4jLogger()
        modules(koinModule)
    }
}
