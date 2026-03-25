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

package org.fog_rock.frlineagent.sampleapp.domain.service

import org.fog_rock.frlineagent.core.domain.model.Notification
import org.fog_rock.frlineagent.core.domain.model.webhook.EventType
import org.fog_rock.frlineagent.core.domain.model.webhook.LineWebhookEvent
import org.fog_rock.frlineagent.core.domain.model.webhook.MessageType
import org.fog_rock.frlineagent.core.domain.model.webhook.SourceType
import org.fog_rock.frlineagent.core.domain.service.AbstractLineBotService
import org.fog_rock.frlineagent.core.domain.service.LineClient
import org.fog_rock.frlineagent.core.domain.service.SignatureVerifier
import org.fog_rock.frlineagent.sampleapp.domain.repository.SheetsRepository
import org.slf4j.LoggerFactory

/**
 * Service class for handling LINE Bot operations.
 */
class LineBotService(
    private val sheetsRepo: SheetsRepository,
    lineClient: LineClient,
    verifier: SignatureVerifier
) : AbstractLineBotService(lineClient, verifier) {
    private val logger = LoggerFactory.getLogger(LineBotService::class.java)

    companion object {
        // Default range for scheduled push notifications (To, Message)
        private const val SHEET_RANGE_PUSH = "Sheet1!A:B"
        // Default range for webhook data retrieval
        private const val SHEET_RANGE_WEBHOOK = "Sheet1!C:D"
    }

    override fun createPushNotifications(): List<Notification> {
        // Fetch sheet data
        val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_PUSH)
        if (sheetData.isEmpty()) {
            logger.info("No data found for scheduled push.")
            return emptyList()
        }

        // Parse & Extract Notification Data
        return sheetData.mapNotNull { row ->
            if (row.size >= 2) {
                Notification(
                    to = row[0].toString(),
                    message = row[1].toString()
                )
            } else {
                null
            }
        }
    }

    override fun createReplyMessage(event: LineWebhookEvent.Event, botId: String): String? {
        if (!shouldReply(event, botId)) {
            logger.info("Should not reply to the event.")
            return null
        }

        // Request Data from Sheets
        val sheetData = sheetsRepo.fetchSheetData(SHEET_RANGE_WEBHOOK)
        if (sheetData.isEmpty()) {
            logger.info("No reply message found in sheet.")
            return null
        }

        // Compose Success Message
        val source = event.source
        val sourceId = when (source?.sourceType) {
            SourceType.USER -> "UserId: ${source.userId}"
            SourceType.GROUP -> "GroupId: ${source.groupId}"
            else -> "SourceId: unknown"
        }
        // Just return the message string. The base class will send it.
        return "Reply message: ${sheetData[0][0]} ($sourceId)"
    }

    private fun shouldReply(event: LineWebhookEvent.Event, botId: String): Boolean {
        if (event.eventType != EventType.MESSAGE) {
            logger.info("The event type is not message. eventType: ${event.eventType}")
            return false
        }
        val message = event.message
        if (message?.messageType != MessageType.TEXT) {
            logger.info("The message type is not text. messageType: ${message?.messageType}")
            return false
        }
        val source = event.source
        logger.info("sourceType: ${source?.sourceType}")
        return when (source?.sourceType) {
            SourceType.USER -> true
            SourceType.GROUP -> message.mention?.mentionees?.any { it.userId == botId } ?: false
            else -> false
        }
    }
}
