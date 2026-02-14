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

package org.fog_rock.frlineagent.presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import org.fog_rock.frlineagent.domain.service.LineBotService
import org.slf4j.LoggerFactory

/**
 * Route handler for LINE Webhook requests.
 */
class WebhookRoute(
    private val service: LineBotService
) {
    private val logger = LoggerFactory.getLogger(WebhookRoute::class.java)

    /**
     * Handles the webhook request.
     *
     * @param call The application call.
     */
    suspend fun handle(call: ApplicationCall) {
        val signature = call.request.header("X-Line-Signature") ?: run {
            logger.error("Missing X-Line-Signature header.")
            call.respond(HttpStatusCode.BadRequest, "Missing X-Line-Signature header.")
            return
        }

        val body = try {
            call.receiveText()
        } catch (e: Exception) {
            logger.error("Failed to receive request body.", e)
            call.respond(HttpStatusCode.InternalServerError, "Failed to receive request body.")
            return
        }

        service.handleWebhook(body, signature)
            .onSuccess {
                call.respond(HttpStatusCode.OK)
            }
            .onFailure { e ->
                if (e is SecurityException) {
                    logger.warn("Invalid signature.", e)
                    call.respond(HttpStatusCode.Unauthorized, "Invalid signature.")
                } else {
                    logger.error("Failed to handle webhook.", e)
                    call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                }
            }
    }
}
