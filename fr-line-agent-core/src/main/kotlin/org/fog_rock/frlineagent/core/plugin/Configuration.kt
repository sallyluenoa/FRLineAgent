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

import org.fog_rock.frlineagent.core.domain.config.ProviderMode
import org.fog_rock.frlineagent.core.domain.service.AbstractLineBotService
import org.koin.core.module.Module
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * A configuration for the FRLineAgent plugin.
 */
class Configuration {
    /**
     * The mode for providing secrets (e.g., from Google Secret Manager or mock).
     * Defaults to [ProviderMode.CLOUD].
     */
    var secretManagerMode: ProviderMode = ProviderMode.CLOUD

    /**
     * The mode for interacting with the LINE API (e.g., actual cloud API or mock).
     * Defaults to [ProviderMode.CLOUD].
     */
    var lineApiMode: ProviderMode = ProviderMode.CLOUD

    /**
     * The Google Cloud Project number. Required when [secretManagerMode] is [ProviderMode.CLOUD].
     */
    var googleCloudProjectNumber: String? = null

    /**
     * The key for retrieving the LINE Bot channel access token from Secret Manager.
     */
    var lineBotChannelAccessTokenKey: String? = null

    /**
     * The key for retrieving the LINE Bot channel secret from Secret Manager.
     */
    var lineBotChannelSecretKey: String? = null

    /**
     * The implementation class of [AbstractLineBotService] that contains the bot's business logic.
     * This property must be set during the plugin installation.
     */
    var lineBotService: KClass<out AbstractLineBotService> by Delegates.notNull()

    /**
     * An application-specific Koin module to be loaded along with the core module.
     */
    var appModule: Module? = null
}
