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

package org.fog_rock.frlineagent.domain.model.webhook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Data class representing the LINE Webhook event object.
 *
 * @property destination The user ID of the bot that should receive the webhook event.
 * @property events The list of webhook events.
 */
@Serializable
data class LineWebhookEvent(
    val destination: String,
    val events: List<Event>
) {
    /**
     * Data class representing a single event in the webhook payload.
     */
    @Serializable
    data class Event(
        @SerialName("type") private val _type: String,
        val replyToken: String? = null,
        val source: Source? = null,
        val timestamp: Long,
        val mode: String,
        val webhookEventId: String,
        val deliveryContext: DeliveryContext,
        val message: Message? = null
    ) {
        @Transient
        val eventType: EventType = EventType.fromString(_type)
    }

    /**
     * Data class representing the source of the event.
     */
    @Serializable
    data class Source(
        @SerialName("type") private val _type: String,
        val userId: String? = null,
        val groupId: String? = null,
        val roomId: String? = null
    ) {
        @Transient
        val sourceType: SourceType = SourceType.fromString(_type)
    }

    /**
     * Data class representing the delivery context.
     */
    @Serializable
    data class DeliveryContext(
        val isRedelivery: Boolean
    )

    /**
     * Data class representing a message object in the event.
     */
    @Serializable
    data class Message(
        val id: String,
        @SerialName("type") private val _type: String,
        val text: String? = null,
        val mention: Mention? = null
    ) {
        @Transient
        val messageType: MessageType = MessageType.fromString(_type)
    }

    /**
     * Data class representing a mention object in the message.
     */
    @Serializable
    data class Mention(
        val mentionees: List<Mentionee>
    )

    /**
     * Data class representing a mentionee object.
     */
    @Serializable
    data class Mentionee(
        val index: Int,
        val length: Int,
        val userId: String
    )
}
