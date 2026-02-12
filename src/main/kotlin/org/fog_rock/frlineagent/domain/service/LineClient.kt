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

/**
 * Interface for LINE Messaging API client.
 */
interface LineClient {
    /**
     * Replies to a message.
     *
     * @param token The reply token.
     * @param message The message to reply.
     * @return Result<Unit> indicating success or failure.
     */
    fun reply(token: String, message: String): Result<Unit>

    /**
     * Pushes a message to a user, group, or room.
     *
     * @param to The ID of the recipient (user, group, or room).
     * @param message The message to push.
     * @return Result<Unit> indicating success or failure.
     */
    fun push(to: String, message: String): Result<Unit>
}
