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

package org.fog_rock.frlineagent.sampleapp.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.fog_rock.frlineagent.sampleapp.domain.config.AppConfig
import org.fog_rock.frlineagent.core.domain.repository.SecretProvider
import org.fog_rock.frlineagent.sampleapp.domain.repository.SheetsRepository
import org.fog_rock.frlineagent.sampleapp.domain.service.LineBotService
import org.fog_rock.frlineagent.core.domain.service.LineClient
import org.fog_rock.frlineagent.core.domain.service.SignatureVerifier
import org.fog_rock.frlineagent.sampleapp.infrastructure.config.KtorAppConfig
import org.fog_rock.frlineagent.sampleapp.infrastructure.external.SecretManagerProvider
import org.fog_rock.frlineagent.sampleapp.infrastructure.repository.GoogleSheetsRepositoryImpl
import org.fog_rock.frlineagent.sampleapp.infrastructure.service.LineMessagingClientImpl
import org.fog_rock.frlineagent.sampleapp.infrastructure.service.LineSignatureVerifierImpl
import org.fog_rock.frlineagent.sampleapp.presentation.PushTriggerRoute
import org.fog_rock.frlineagent.sampleapp.presentation.WebhookRoute
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        val koinModule = module {
            single<AppConfig> { KtorAppConfig(environment.config) }
            single<SecretProvider> { SecretManagerProvider(get()) }
            single<SheetsRepository> { GoogleSheetsRepositoryImpl(get(), get()) }
            single<LineClient> { LineMessagingClientImpl(get(), get()) }
            single<SignatureVerifier> { LineSignatureVerifierImpl(get(), get()) }
            single { LineBotService(get(), get(), get()) }
            single { WebhookRoute(get()) }
            single { PushTriggerRoute(get()) }
        }
        modules(koinModule)
    }
}
