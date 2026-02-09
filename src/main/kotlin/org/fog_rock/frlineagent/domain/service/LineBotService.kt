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
import org.fog_rock.frlineagent.domain.model.LineWebhookEvent
import org.fog_rock.frlineagent.domain.model.NotificationContent
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
        // 1. Verify Signature
        if (!verifier.verify(body, signature)) {
            val msg = "Invalid signature."
            logger.error(msg)
            return Result.failure(SecurityException(msg))
        }

        // 2. Parse & Process Data
        val webhookEvent = try {
            json.decodeFromString<LineWebhookEvent>(body)
        } catch (e: Exception) {
            logger.error("Failed to parse webhook event.", e)
            return Result.failure(e)
        }

        // Launch background worker asynchronously
        scope.launch {
            webhookEvent.events.forEach { event ->
                val replyToken = event.replyToken
                if (replyToken != null) {
                    try {
                        // 3. Request Data from Sheets
                        val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_WEBHOOK)

                        // 4. Compose Success Message
                        val message = if (sheetData.isNotEmpty()) {
                            "Received webhook. Found ${sheetData.size} rows in sheets."
                        } else {
                            "Received webhook. No data found in sheets."
                        }

                        // 5. Call Reply API
                        lineClient.reply(replyToken, message)
                    } catch (e: Exception) {
                        logger.error("Error processing webhook in background", e)
                    }
                } else {
                    logger.info("Event does not have a replyToken. Type: ${event.type}")
                }
            }
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
        return try {
            // 1. Fetch sheet data
            val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_PUSH)

            if (sheetData.isEmpty()) {
                logger.info("No data found for scheduled push.")
                return Result.success(Unit)
            }

            // 2. Parse & Extract Notification Data
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

            // 3. Call Push Message API
            var failureCount = 0
            val totalCount = notifications.size

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

            if (failureCount > 0) {
                val errorMsg = "Push notifications completed with errors. Failed: $failureCount / Total: $totalCount"
                logger.error(errorMsg)
                Result.failure(RuntimeException(errorMsg))
            } else {
                logger.info("All $totalCount push notifications sent successfully.")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            logger.error("Error executing scheduled push", e)
            Result.failure(e)
        }
    }
}
