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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.fog_rock.frlineagent.core.domain.model.Notification
import org.fog_rock.frlineagent.core.domain.model.webhook.LineWebhookEvent
import org.slf4j.LoggerFactory

/**
 * Abstract base class for handling LINE Bot operations.
 * Provides a framework for webhook handling and push messaging.
 */
abstract class AbstractLineBotService(
    protected val lineClient: LineClient,
    protected val verifier: SignatureVerifier
) {
    private val logger = LoggerFactory.getLogger(AbstractLineBotService::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Handles the webhook request from LINE Platform.
     * Verifies the signature, parses the event, and delegates to handleEvent.
     *
     * @param body The request body.
     * @param signature The signature from the X-Line-Signature header.
     * @return Result<Unit> indicating success or failure of verification/parsing.
     */
    fun handleWebhook(body: String, signature: String): Result<Unit> {
        if (!verifySignature(body, signature)) {
            val e = SecurityException("Invalid signature.")
            logger.error(e.message)
            return Result.failure(e)
        }

        val webhookEvent = try {
            json.decodeFromString<LineWebhookEvent>(body)
        } catch (e: Exception) {
            logger.error("Failed to parse webhook event.", e)
            return Result.failure(e)
        }

        scope.launch {
            webhookEvent.events.forEach { processEvent(it, webhookEvent.destination) }
        }

        return Result.success(Unit)
    }

    /**
     * Executes the push notification process.
     * It retrieves notifications from `createPushNotifications` and sends them.
     *
     * @return Result<Unit> indicating success or failure of the overall operation.
     */
    fun executePush(): Result<Unit> {
        val notifications = createPushNotifications()
        if (notifications.isEmpty()) {
            logger.info("No notifications to push.")
            return Result.success(Unit)
        }

        val failureCount = pushAll(notifications)

        return if (failureCount > 0) {
            val e = RuntimeException(
                "Push notifications completed with errors. Failed: $failureCount / Total: ${notifications.size}"
            )
            logger.error(e.message)
            Result.failure(e)
        } else {
            logger.info("All ${notifications.size} push notifications sent successfully.")
            Result.success(Unit)
        }
    }

    /**
     * Verifies the webhook signature.
     * This method can be overridden in subclasses for custom verification logic, e.g., for debugging.
     *
     * @param body The request body.
     * @param signature The signature from the X-Line-Signature header.
     * @return True if the signature is valid, false otherwise.
     */
    protected open fun verifySignature(body: String, signature: String): Boolean =
        verifier.verify(body, signature)

    /**
     * Creates a reply message for a given webhook event.
     * Subclasses must implement this method to define the bot's reply logic.
     * If null is returned, no reply message will be sent.
     *
     * @param event The webhook event.
     * @param botId The destination bot ID.
     * @return A reply message string, or null if no reply should be sent.
     */
    protected abstract fun createReplyMessage(event: LineWebhookEvent.Event, botId: String): String?

    /**
     * Creates a list of push notifications.
     * Subclasses must implement this method to define the bot's push notification logic.
     *
     * @return A list of notifications to be sent.
     */
    protected abstract fun createPushNotifications(): List<Notification>

    /**
     * Processes a single webhook event.
     * It retrieves a reply message from `createReplyMessage` and sends it if available.
     *
     * @param event The webhook event to process.
     * @param botId The destination bot ID.
     */
    private fun processEvent(event: LineWebhookEvent.Event, botId: String) {
        val replyToken = event.replyToken ?: return // No token, no reply.

        // Call the abstract method to get the message from the subclass
        val message = createReplyMessage(event, botId)

        // If the subclass provided a message, send the reply.
        if (!message.isNullOrBlank()) {
            reply(replyToken, message)
        }
    }

    private fun reply(replyToken: String, message: String): Result<Unit> =
        lineClient.reply(replyToken, message)

    private fun pushAll(notifications: List<Notification>): Int {
        var failureCount = 0
        notifications.forEach { notification ->
            push(notification.to, notification.message)
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

    private fun push(to: String, message: String): Result<Unit> =
        lineClient.push(to, message)
}
