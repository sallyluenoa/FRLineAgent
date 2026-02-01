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

package org.fog_rock.frlineagent.infrastructure.repository

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.fog_rock.frlineagent.domain.model.NotificationContent
import org.fog_rock.frlineagent.domain.repository.SheetsRepository
import org.fog_rock.frlineagent.infrastructure.external.SecretManagerProvider
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

class GoogleSheetsRepositoryImpl(
    private val secretManagerProvider: SecretManagerProvider
) : SheetsRepository {

    private val logger = LoggerFactory.getLogger(GoogleSheetsRepositoryImpl::class.java)

    companion object {
        private const val APPLICATION_NAME = "FRLineAgent"
        private const val SPREADSHEET_ID_KEY = "SPREADSHEET_ID"
        private const val GOOGLE_CREDENTIALS_KEY = "GOOGLE_CREDENTIALS_JSON"
        private const val SHEET_RANGE = "Notifications!A2:B" // Assuming data starts from A2
    }

    override fun fetchNotificationData(): List<NotificationContent> {
        return try {
            val spreadsheetId = secretManagerProvider.getSecret(SPREADSHEET_ID_KEY)
            val credentialsJson = secretManagerProvider.getSecret(GOOGLE_CREDENTIALS_KEY)

            val service = createSheetsService(credentialsJson)
            val response = service.spreadsheets().values()
                .get(spreadsheetId, SHEET_RANGE)
                .execute()

            val values = response.getValues()
            if (values == null || values.isEmpty()) {
                logger.info("No data found in spreadsheet.")
                emptyList()
            } else {
                values.mapNotNull { row ->
                    if (row.size >= 2) {
                        NotificationContent(
                            userId = row[0].toString(),
                            message = row[1].toString()
                        )
                    } else {
                        logger.warn("Skipping invalid row: $row")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch notification data from Google Sheets", e)
            emptyList()
        }
    }

    private fun createSheetsService(credentialsJson: String): Sheets {
        val jsonFactory = GsonFactory.getDefaultInstance()
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        val credential = GoogleCredential.fromStream(ByteArrayInputStream(credentialsJson.toByteArray()))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}
