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

import com.linecorp.bot.client.base.Result
import com.linecorp.bot.messaging.client.MessagingApiClient
import com.linecorp.bot.messaging.model.PushMessageRequest
import com.linecorp.bot.messaging.model.PushMessageResponse
import com.linecorp.bot.messaging.model.ReplyMessageRequest
import com.linecorp.bot.messaging.model.ReplyMessageResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LineMessagingCloudClientTest {

    private lateinit var appConfig: AppConfig
    private lateinit var secretProvider: SecretProvider
    private lateinit var messagingApiClient: MessagingApiClient
    private lateinit var client: LineMessagingCloudClient

    @BeforeEach
    fun setUp() {
        appConfig = mockk()
        secretProvider = mockk()
        messagingApiClient = mockk()

        every { appConfig.lineBotChannelAccessTokenKey } returns "test-token-key"
        every { secretProvider.getSecret("test-token-key") } returns "test-token"

        mockkStatic(MessagingApiClient::class)
        val builder = mockk<com.linecorp.bot.client.base.ApiAuthenticatedClientBuilder<MessagingApiClient>>()
        every { MessagingApiClient.builder("test-token") } returns builder
        every { builder.build() } returns messagingApiClient

        client = LineMessagingCloudClient(appConfig, secretProvider)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testReply_success() {
        val token = "reply-token"
        val message = "Hello"
        val result = mockk<Result<ReplyMessageResponse>>()
        val future = CompletableFuture.completedFuture(result)

        every { messagingApiClient.replyMessage(any<ReplyMessageRequest>()) } returns future

        val clientResult = client.reply(token, message)

        assertTrue(clientResult.isSuccess)
        verify { messagingApiClient.replyMessage(any<ReplyMessageRequest>()) }
    }

    @Test
    fun testReply_failure() {
        val token = "reply-token"
        val message = "Hello"
        val future = CompletableFuture<Result<ReplyMessageResponse>>()
        future.completeExceptionally(RuntimeException("API Error"))

        every { messagingApiClient.replyMessage(any<ReplyMessageRequest>()) } returns future

        val clientResult = client.reply(token, message)

        assertTrue(clientResult.isFailure)
    }

    @Test
    fun testPush_success() {
        val userId = "user-id"
        val message = "Hello"
        val result = mockk<Result<PushMessageResponse>>()
        val future = CompletableFuture.completedFuture(result)

        every { messagingApiClient.pushMessage(any<UUID>(), any<PushMessageRequest>()) } returns future

        val clientResult = client.push(userId, message)

        assertTrue(clientResult.isSuccess)
        verify { messagingApiClient.pushMessage(any<UUID>(), any<PushMessageRequest>()) }
    }

    @Test
    fun testPush_failure() {
        val userId = "user-id"
        val message = "Hello"
        val future = CompletableFuture<Result<PushMessageResponse>>()
        future.completeExceptionally(RuntimeException("API Error"))

        every { messagingApiClient.pushMessage(any<UUID>(), any<PushMessageRequest>()) } returns future

        val clientResult = client.push(userId, message)

        assertTrue(clientResult.isFailure)
    }
}
