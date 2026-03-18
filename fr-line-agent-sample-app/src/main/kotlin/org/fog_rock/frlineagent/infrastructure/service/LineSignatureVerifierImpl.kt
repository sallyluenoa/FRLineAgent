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

package org.fog_rock.frlineagent.infrastructure.service

import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.config.ProviderMode
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.fog_rock.frlineagent.domain.service.SignatureVerifier
import org.fog_rock.frlineagent.infrastructure.internal.cloud.LineSignatureCloudVerifier
import org.fog_rock.frlineagent.infrastructure.internal.mock.LineSignatureMockVerifier

class LineSignatureVerifierImpl(
    appConfig: AppConfig,
    secretManagerProvider: SecretProvider
) : SignatureVerifier {

    private val verifier: SignatureVerifier = when (appConfig.lineApiMode) {
        ProviderMode.CLOUD -> LineSignatureCloudVerifier(appConfig, secretManagerProvider)
        ProviderMode.MOCK -> LineSignatureMockVerifier()
    }

    override fun verify(body: String, signature: String): Boolean = verifier.verify(body, signature)
}
