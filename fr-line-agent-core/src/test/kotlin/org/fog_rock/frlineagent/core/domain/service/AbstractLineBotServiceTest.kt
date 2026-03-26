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

package org.fog_rock.frlineagent.core.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.fog_rock.frlineagent.core.domain.model.Notification
import org.fog_rock.frlineagent.core.domain.model.webhook.LineWebhookEvent
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AbstractLineBotServiceTest {

    private lateinit var lineClient: LineClient
    private lateinit var verifier: SignatureVerifier
    private lateinit var service: TestLineBotService

    // Test Data
    private val botId = "U_BOT_ID"
    private val validSignature = "valid_signature"
    private val json = Json { ignoreUnknownKeys = true }

    // Concrete implementation for testing
    private class TestLineBotService(
        lineClient: LineClient,
        verifier: SignatureVerifier
    ) : AbstractLineBotService(lineClient, verifier) {
        var replyMessage: String? = "Default Reply"
        var notifications: List<Notification> = emptyList()

        override fun createReplyMessage(event: LineWebhookEvent.Event, botId: String): String? = replyMessage
        override fun createPushNotifications(): List<Notification> = notifications
    }

    @BeforeEach
    fun setUp() {
        lineClient = mockk(relaxed = true)
        verifier = mockk(relaxed = true)
        service = TestLineBotService(lineClient, verifier)
    }

    private fun createDummyEvent(replyToken: String? = "replyToken"): LineWebhookEvent.Event {
        return LineWebhookEvent.Event(
            _type = "message",
            replyToken = replyToken,
            source = null,
            timestamp = 1234567890,
            mode = "active",
            webhookEventId = "webhookEventId",
            deliveryContext = LineWebhookEvent.DeliveryContext(false),
            message = null
        )
    }

    private fun createWebhookJson(vararg events: LineWebhookEvent.Event): String {
        val webhook = LineWebhookEvent(botId, events.toList())
        return json.encodeToString(webhook)
    }

    @Test
    fun testHandleWebhook_invalidSignature() {
        // Arrange
        every { verifier.verify(any(), any()) } returns false
        val body = createWebhookJson(createDummyEvent())

        // Act
        val result = service.handleWebhook(body, "invalid_signature")

        // Assert
        assertTrue(result.isFailure)
        verify(exactly = 0) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testHandleWebhook_invalidJson() {
        // Arrange
        every { verifier.verify(any(), any()) } returns true
        val body = "{invalid json"

        // Act
        val result = service.handleWebhook(body, validSignature)

        // Assert
        assertTrue(result.isFailure)
        verify(exactly = 0) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testHandleWebhook_successWithReply() {
        // Arrange
        every { verifier.verify(any(), any()) } returns true
        val event = createDummyEvent("test_token")
        val body = createWebhookJson(event)
        service.replyMessage = "Test Reply"
        every { lineClient.reply(any(), any()) } returns Result.success(Unit)

        // Act
        val result = service.handleWebhook(body, validSignature)

        // Assert
        assertTrue(result.isSuccess)
        verify(timeout = 1000) { lineClient.reply("test_token", "Test Reply") }
    }

    @Test
    fun testHandleWebhook_successNoReplyMessage() {
        // Arrange
        every { verifier.verify(any(), any()) } returns true
        val body = createWebhookJson(createDummyEvent())
        service.replyMessage = null // No reply to be generated

        // Act
        val result = service.handleWebhook(body, validSignature)

        // Assert
        assertTrue(result.isSuccess)
        // Verify with a small timeout to allow coroutine to run, but expect no call
        verify(exactly = 0, timeout = 500) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testHandleWebhook_successNoReplyToken() {
        // Arrange
        every { verifier.verify(any(), any()) } returns true
        val event = createDummyEvent(replyToken = null) // Event without a reply token
        val body = createWebhookJson(event)
        service.replyMessage = "This should not be sent"

        // Act
        val result = service.handleWebhook(body, validSignature)

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 0, timeout = 500) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testExecutePush_successWithNotifications() {
        // Arrange
        val notifications = listOf(
            Notification("user1", "message1"),
            Notification("user2", "message2")
        )
        service.notifications = notifications
        every { lineClient.push(any(), any()) } returns Result.success(Unit)

        // Act
        val result = service.executePush()

        // Assert
        assertTrue(result.isSuccess)
        verify { lineClient.push("user1", "message1") }
        verify { lineClient.push("user2", "message2") }
    }

    @Test
    fun testExecutePush_successNoNotifications() {
        // Arrange
        service.notifications = emptyList() // No notifications to send

        // Act
        val result = service.executePush()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 0) { lineClient.push(any(), any()) }
    }

    @Test
    fun testExecutePush_failurePartial() {
        // Arrange
        val notifications = listOf(
            Notification("user1", "message1"),
            Notification("user2", "message2")
        )
        service.notifications = notifications
        every { lineClient.push("user1", "message1") } returns Result.success(Unit)
        every { lineClient.push("user2", "message2") } returns Result.failure(RuntimeException("Push Error"))

        // Act
        val result = service.executePush()

        // Assert
        assertTrue(result.isFailure)
        verify { lineClient.push("user1", "message1") }
        verify { lineClient.push("user2", "message2") }
    }
}
