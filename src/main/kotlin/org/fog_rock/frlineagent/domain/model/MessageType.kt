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
 * Enum class representing the type of a LINE message.
 *
 * @property value The string value of the message type as defined by LINE.
 */
@Serializable
enum class MessageType(val value: String) {
    /** Text message. */
    TEXT("text"),
    /** Image message. */
    IMAGE("image"),
    /** Video message. */
    VIDEO("video"),
    /** Audio message. */
    AUDIO("audio"),
    /** File message. */
    FILE("file"),
    /** Location message. */
    LOCATION("location"),
    /** Sticker message. */
    STICKER("sticker"),
    /** Unknown message type. */
    UNKNOWN("unknown");

    companion object {
        /**
         * Converts a string value to a MessageType.
         *
         * @param value The string value to convert.
         * @return The corresponding MessageType.
         */
        fun fromString(value: String?): MessageType = entries.find { it.value == value } ?: UNKNOWN
    }
}
