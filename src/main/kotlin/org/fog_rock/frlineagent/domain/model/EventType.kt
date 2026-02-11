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
 * Enum class representing the type of a LINE Webhook event.
 *
 * @property value The string value of the event type as defined by LINE.
 */
@Serializable
enum class EventType(val value: String) {
    /** Message event. */
    MESSAGE("message"),
    /** Follow event. */
    FOLLOW("follow"),
    /** Unfollow event. */
    UNFOLLOW("unfollow"),
    /** Join event. */
    JOIN("join"),
    /** Leave event. */
    LEAVE("leave"),
    /** Postback event. */
    POSTBACK("postback"),
    /** Beacon event. */
    BEACON("beacon"),
    /** Account link event. */
    ACCOUNT_LINK("accountLink"),
    /** Member joined event. */
    MEMBER_JOINED("memberJoined"),
    /** Member left event. */
    MEMBER_LEFT("memberLeft"),
    /** Things event. */
    THINGS("things"),
    /** Unknown event type. */
    UNKNOWN("unknown");

    companion object {
        /**
         * Converts a string value to an EventType.
         *
         * @param value The string value to convert.
         * @return The corresponding EventType.
         */
        fun fromString(value: String?): EventType = entries.find { it.value == value } ?: UNKNOWN
    }
}
