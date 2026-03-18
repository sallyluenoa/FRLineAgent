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

package org.fog_rock.frlineagent.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.fog_rock.frlineagent.domain.repository.SheetsRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LineBotServiceTest {

    private lateinit var sheetsRepo: SheetsRepository
    private lateinit var lineClient: LineClient
    private lateinit var verifier: SignatureVerifier
    private lateinit var service: LineBotService

    @BeforeEach
    fun setUp() {
        sheetsRepo = mockk()
        lineClient = mockk()
        verifier = mockk()
        service = LineBotService(sheetsRepo, lineClient, verifier)
    }

    @Test
    fun testHandleWebhook_invalidSignature() {
        every { verifier.verify(any(), any()) } returns false

        val result = service.handleWebhook("{}", "invalid_signature")

        assertTrue(result.isFailure)
        verify(exactly = 0) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testHandleWebhook_invalidJson() {
        every { verifier.verify(any(), any()) } returns true

        val result = service.handleWebhook("invalid json", "signature")

        assertTrue(result.isFailure)
        verify(exactly = 0) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testHandleWebhook_validTextMessage() {
        every { verifier.verify(any(), any()) } returns true
        every { sheetsRepo.fetchSheetData(any()) } returns listOf(listOf("Reply Content"))
        every { lineClient.reply(any(), any()) } returns Result.success(Unit)

        val body = """
            {
              "destination": "destination_id",
              "events": [
                {
                  "type": "message",
                  "replyToken": "token",
                  "source": { "type": "user", "userId": "user1" },
                  "timestamp": 1234567890,
                  "mode": "active",
                  "webhookEventId": "id",
                  "deliveryContext": { "isRedelivery": false },
                  "message": { "id": "msg1", "type": "text", "text": "hello" }
                }
              ]
            }
        """.trimIndent()

        val result = service.handleWebhook(body, "signature")

        assertTrue(result.isSuccess)
        verify(timeout = 1000) { lineClient.reply("token", any()) }
    }

    @Test
    fun testHandleWebhook_notMessageEvent() {
        every { verifier.verify(any(), any()) } returns true

        val body = """
            {
              "destination": "destination_id",
              "events": [
                {
                  "type": "follow",
                  "replyToken": "token",
                  "source": { "type": "user", "userId": "user1" },
                  "timestamp": 1234567890,
                  "mode": "active",
                  "webhookEventId": "id",
                  "deliveryContext": { "isRedelivery": false }
                }
              ]
            }
        """.trimIndent()

        val result = service.handleWebhook(body, "signature")

        assertTrue(result.isSuccess)
        verify(exactly = 0, timeout = 1000) { lineClient.reply(any(), any()) }
    }

    @Test
    fun testExecuteScheduledPush_emptyData() {
        every { sheetsRepo.fetchSheetData(any()) } returns emptyList()

        val result = service.executeScheduledPush()

        assertTrue(result.isSuccess)
        verify(exactly = 0) { lineClient.push(any(), any()) }
    }

    @Test
    fun testExecuteScheduledPush_validRows() {
        every { sheetsRepo.fetchSheetData(any()) } returns listOf(
            listOf("user1", "message1"),
            listOf("user2", "message2")
        )
        every { lineClient.push(any(), any()) } returns Result.success(Unit)

        val result = service.executeScheduledPush()

        assertTrue(result.isSuccess)
        verify { lineClient.push("user1", "message1") }
        verify { lineClient.push("user2", "message2") }
    }

    @Test
    fun testExecuteScheduledPush_partialFailure() {
        every { sheetsRepo.fetchSheetData(any()) } returns listOf(
            listOf("user1", "message1"),
            listOf("user2", "message2")
        )
        every { lineClient.push("user1", "message1") } returns Result.success(Unit)
        every { lineClient.push("user2", "message2") } returns Result.failure(RuntimeException("Error"))

        val result = service.executeScheduledPush()

        assertTrue(result.isFailure)
        verify { lineClient.push("user1", "message1") }
        verify { lineClient.push("user2", "message2") }
    }
}
