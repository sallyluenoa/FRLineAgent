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
     * Verifies the webhook signature.
     * This method can be overridden in subclasses for custom verification logic, e.g., for debugging.
     *
     * @param body The request body.
     * @param signature The signature from the X-Line-Signature header.
     * @return True if the signature is valid, false otherwise.
     */
    protected open fun verifySignature(body: String, signature: String): Boolean =
        verifier.verify(body, signature)

    private fun processEvent(event: LineWebhookEvent.Event, botId: String) {
        val replyToken = event.replyToken ?: return // No token, no reply.

        // Call the abstract method to get the message from the subclass
        val message = createReplyMessage(event, botId)

        // If the subclass provided a message, send the reply.
        if (!message.isNullOrBlank()) {
            reply(replyToken, message)
        }
    }

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
     * Sends a push message.
     *
     * @param to The recipient ID.
     * @param message The message to send.
     * @return Result<Unit> indicating success or failure.
     */
    protected fun push(to: String, message: String): Result<Unit> =
        lineClient.push(to, message)

    /**
     * Sends multiple push notifications.
     *
     * @param notifications A list of notifications to send.
     * @return The number of failed pushes.
     */
    protected fun pushAll(notifications: List<Notification>): Int {
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

    /**
     * Sends a reply message.
     *
     * @param replyToken The token for replying.
     * @param message The message to send.
     * @return Result<Unit> indicating success or failure.
     */
    protected fun reply(replyToken: String, message: String): Result<Unit> =
        lineClient.reply(replyToken, message)

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
}
