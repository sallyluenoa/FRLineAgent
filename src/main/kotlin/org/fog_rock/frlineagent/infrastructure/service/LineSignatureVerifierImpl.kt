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

import com.linecorp.bot.parser.LineSignatureValidator
import org.fog_rock.frlineagent.domain.service.SignatureVerifier
import org.fog_rock.frlineagent.infrastructure.external.SecretManagerProvider
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

class LineSignatureVerifierImpl(
    private val secretManagerProvider: SecretManagerProvider
) : SignatureVerifier {

    private val logger = LoggerFactory.getLogger(LineSignatureVerifierImpl::class.java)

    companion object {
        private const val CHANNEL_SECRET_KEY = "LINE_CHANNEL_SECRET"
    }

    override fun verify(body: String, signature: String): Boolean {
        return try {
            val channelSecret = secretManagerProvider.getSecret(CHANNEL_SECRET_KEY)
            val validator = LineSignatureValidator(channelSecret.toByteArray(StandardCharsets.UTF_8))
            validator.validateSignature(body.toByteArray(StandardCharsets.UTF_8), signature)
        } catch (e: Exception) {
            logger.error("Failed to verify signature", e)
            false
        }
    }
}
