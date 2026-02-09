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

package org.fog_rock.frlineagent.domain.model

import kotlinx.serialization.Serializable

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
     *
     * @property type The type of the event.
     * @property replyToken The reply token for replying to the event.
     * @property source The source of the event.
     * @property timestamp The time of the event in milliseconds.
     * @property mode The mode of the channel (active or standby).
     * @property webhookEventId The ID of the webhook event.
     * @property deliveryContext The delivery context.
     */
    @Serializable
    data class Event(
        val type: String,
        val replyToken: String? = null,
        val source: Source? = null,
        val timestamp: Long,
        val mode: String,
        val webhookEventId: String,
        val deliveryContext: DeliveryContext
    )

    /**
     * Data class representing the source of the event.
     *
     * @property type The type of the source (user, group, room).
     * @property userId The ID of the user.
     * @property groupId The ID of the group.
     * @property roomId The ID of the room.
     */
    @Serializable
    data class Source(
        val type: String,
        val userId: String? = null,
        val groupId: String? = null,
        val roomId: String? = null
    )

    /**
     * Data class representing the delivery context.
     *
     * @property isRedelivery Whether the event is a redelivery.
     */
    @Serializable
    data class DeliveryContext(
        val isRedelivery: Boolean
    )
}
