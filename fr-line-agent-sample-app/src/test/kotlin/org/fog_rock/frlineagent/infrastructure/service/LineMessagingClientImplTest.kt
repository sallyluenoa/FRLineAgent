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

package org.fog_rock.frlineagent.infrastructure.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.config.ProviderMode
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.fog_rock.frlineagent.infrastructure.internal.cloud.LineMessagingCloudClient
import org.fog_rock.frlineagent.infrastructure.internal.mock.LineMessagingMockClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LineMessagingClientImplTest {

    private lateinit var appConfig: AppConfig
    private lateinit var secretProvider: SecretProvider

    @BeforeEach
    fun setUp() {
        appConfig = mockk()
        secretProvider = mockk()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testReply_cloudMode() {
        every { appConfig.lineApiMode } returns ProviderMode.CLOUD
        every { appConfig.lineBotChannelAccessTokenKey } returns "test-token-key"
        every { secretProvider.getSecret("test-token-key") } returns "test-token"

        mockkConstructor(LineMessagingCloudClient::class)
        every { anyConstructed<LineMessagingCloudClient>().reply(any(), any()) } returns Result.success(Unit)

        val client = LineMessagingClientImpl(appConfig, secretProvider)
        val result = client.reply("token", "message")

        assertTrue(result.isSuccess)
        verify { anyConstructed<LineMessagingCloudClient>().reply("token", "message") }
    }

    @Test
    fun testReply_mockMode() {
        every { appConfig.lineApiMode } returns ProviderMode.MOCK

        mockkConstructor(LineMessagingMockClient::class)
        every { anyConstructed<LineMessagingMockClient>().reply(any(), any()) } returns Result.success(Unit)

        val client = LineMessagingClientImpl(appConfig, secretProvider)
        val result = client.reply("token", "message")

        assertTrue(result.isSuccess)
        verify { anyConstructed<LineMessagingMockClient>().reply("token", "message") }
    }

    @Test
    fun testPush_cloudMode() {
        every { appConfig.lineApiMode } returns ProviderMode.CLOUD
        every { appConfig.lineBotChannelAccessTokenKey } returns "test-token-key"
        every { secretProvider.getSecret("test-token-key") } returns "test-token"

        mockkConstructor(LineMessagingCloudClient::class)
        every { anyConstructed<LineMessagingCloudClient>().push(any(), any()) } returns Result.success(Unit)

        val client = LineMessagingClientImpl(appConfig, secretProvider)
        val result = client.push("userId", "message")

        assertTrue(result.isSuccess)
        verify { anyConstructed<LineMessagingCloudClient>().push("userId", "message") }
    }

    @Test
    fun testPush_mockMode() {
        every { appConfig.lineApiMode } returns ProviderMode.MOCK

        mockkConstructor(LineMessagingMockClient::class)
        every { anyConstructed<LineMessagingMockClient>().push(any(), any()) } returns Result.success(Unit)

        val client = LineMessagingClientImpl(appConfig, secretProvider)
        val result = client.push("userId", "message")

        assertTrue(result.isSuccess)
        verify { anyConstructed<LineMessagingMockClient>().push("userId", "message") }
    }
}
