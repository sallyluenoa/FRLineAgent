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

package org.fog_rock.frlineagent.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.infrastructure.config.KtorAppConfig
import org.fog_rock.frlineagent.infrastructure.external.SecretManagerProvider
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        val koinModule = module {
            single<AppConfig> { KtorAppConfig(environment.config) }
            single { SecretManagerProvider(get()) }
        }
        modules(koinModule)
    }
}
