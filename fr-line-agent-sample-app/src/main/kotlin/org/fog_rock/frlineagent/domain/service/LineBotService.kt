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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.fog_rock.frlineagent.domain.model.webhook.EventType
import org.fog_rock.frlineagent.domain.model.webhook.LineWebhookEvent
import org.fog_rock.frlineagent.domain.model.webhook.MessageType
import org.fog_rock.frlineagent.domain.model.notification.NotificationContent
import org.fog_rock.frlineagent.domain.model.webhook.SourceType
import org.fog_rock.frlineagent.domain.repository.SheetsRepository
import org.slf4j.LoggerFactory

/**
 * Service class for handling LINE Bot operations.
 */
class LineBotService(
    private val sheetsRepo: SheetsRepository,
    private val lineClient: LineClient,
    private val verifier: SignatureVerifier
) {
    private val logger = LoggerFactory.getLogger(LineBotService::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        // Default range for scheduled push notifications (To, Message)
        private const val SHEET_RANGE_PUSH = "Sheet1!A:B"
        // Default range for webhook data retrieval
        private const val SHEET_RANGE_WEBHOOK = "Sheet1!C:D"
    }

    /**
     * Handles the webhook request from LINE Platform.
     *
     * @param body The request body.
     * @param signature The signature from the X-Line-Signature header.
     * @return Result<Unit> indicating success or failure.
     */
    fun handleWebhook(body: String, signature: String): Result<Unit> {
        // Verify Signature
        if (!verifier.verify(body, signature)) {
            val e = SecurityException("Invalid signature.")
            logger.error(e.message)
            return Result.failure(e)
        }

        // Parse & Process Data
        val webhookEvent = try {
            json.decodeFromString<LineWebhookEvent>(body)
        } catch (e: Exception) {
            logger.error("Failed to parse webhook event.", e)
            return Result.failure(e)
        }

        // Launch background worker asynchronously
        scope.launch {
            webhookEvent.events.forEach { reply(it, webhookEvent.destination) }
        }

        // Return success immediately to acknowledge receipt
        return Result.success(Unit)
    }

    /**
     * Executes the scheduled push notification logic.
     *
     * @return Result<Unit> indicating success or failure.
     */
    fun executeScheduledPush(): Result<Unit> {
        // Fetch sheet data
        val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_PUSH)
        if (sheetData.isEmpty()) {
            logger.info("No data found for scheduled push.")
            return Result.success(Unit)
        }

        // Parse & Extract Notification Data
        val notifications = sheetData.mapNotNull { row ->
            if (row.size >= 2) {
                NotificationContent(
                    to = row[0].toString(),
                    message = row[1].toString()
                )
            } else {
                null
            }
        }

        // Call Push Message API
        val failureCount = pushAll(notifications)

        if (failureCount > 0) {
            val e = RuntimeException(
                "Push notifications completed with errors. Failed: $failureCount / Total: ${notifications.size}"
            )
            logger.error(e.message)
            return Result.failure(e)
        }

        logger.info("All ${notifications.size} push notifications sent successfully.")
        return Result.success(Unit)
    }

    private fun reply(event: LineWebhookEvent.Event, botId: String) {
        if (!shouldReply(event, botId)) {
            logger.info("Should not reply the event.")
            return
        }
        val replyToken = event.replyToken ?: run {
            logger.info("Event does not have the replyToken.")
            return
        }
        // Request Data from Sheets
        val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_WEBHOOK)
        if (sheetData.isEmpty()) {
            logger.info("Not found the reply message.")
            return
        }
        // Compose Success Message
        val sourceId = when (event.source?.sourceType) {
            SourceType.USER -> "UserId: ${event.source.userId}"
            SourceType.GROUP -> "GroupId: ${event.source.groupId}"
            else -> "SourceId: unknown"
        }
        val message = "Reply message: ${sheetData[0][0]} ($sourceId)"

        // Call Reply API
        lineClient.reply(replyToken, message)
    }

    private fun shouldReply(event: LineWebhookEvent.Event, botId: String): Boolean {
        if (event.eventType != EventType.MESSAGE) {
            logger.info("The event type is not message. eventType: ${event.eventType}")
            return false
        }
        if (event.message?.messageType != MessageType.TEXT) {
            logger.info("The message type is not text. messageType: ${event.message?.messageType}")
            return false
        }
        logger.info("sourceType: ${event.source?.sourceType}")
        return when (event.source?.sourceType) {
            SourceType.USER -> true
            SourceType.GROUP -> event.message.mention?.mentionees?.any { it.userId == botId } ?: false
            else -> false
        }
    }

    private fun pushAll(notifications: List<NotificationContent>): Int {
        var failureCount = 0
        notifications.forEach { notification ->
            lineClient.push(notification.to, notification.message)
                .onSuccess {
                    logger.info("Successfully pushed message to ${notification.to}")
                }
                .onFailure { e ->
                    logger.error("Failed to push message to ${notification.to}", e)
                    failureCount++
                }
        }
        return failureCount
    }
}
