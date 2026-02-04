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

import com.linecorp.bot.messaging.client.MessagingApiClient
import com.linecorp.bot.messaging.model.PushMessageRequest
import com.linecorp.bot.messaging.model.ReplyMessageRequest
import com.linecorp.bot.messaging.model.TextMessage
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.fog_rock.frlineagent.domain.service.LineClient
import org.slf4j.LoggerFactory
import java.util.UUID

class LineMessagingClientImpl(
    private val secretManagerProvider: SecretProvider
) : LineClient {

    private val logger = LoggerFactory.getLogger(LineMessagingClientImpl::class.java)

    companion object {
        private const val CHANNEL_ACCESS_TOKEN_KEY = "LINE_CHANNEL_ACCESS_TOKEN"
    }

    private val client: MessagingApiClient by lazy {
        val channelAccessToken = secretManagerProvider.getSecret(CHANNEL_ACCESS_TOKEN_KEY)
        MessagingApiClient.builder(channelAccessToken).build()
    }

    override fun reply(token: String, message: String): Result<Unit> {
        return try {
            val replyMessageRequest = ReplyMessageRequest.Builder(token, listOf(TextMessage(message))).build()
            val response = client.replyMessage(replyMessageRequest).get()
            logger.info("Reply message sent: $response")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to reply message", e)
            Result.failure(e)
        }
    }

    override fun push(userId: String, message: String): Result<Unit> {
        return try {
            val pushMessageRequest = PushMessageRequest.Builder(userId, listOf(TextMessage(message))).build()
            val response = client.pushMessage(UUID.randomUUID(), pushMessageRequest).get()
            logger.info("Push message sent: $response")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to push message", e)
            Result.failure(e)
        }
    }
}
