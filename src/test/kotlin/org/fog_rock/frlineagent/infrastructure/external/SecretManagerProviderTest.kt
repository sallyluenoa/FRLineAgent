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

package org.fog_rock.frlineagent.infrastructure.external

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.config.enums.ProviderMode
import org.fog_rock.frlineagent.infrastructure.internal.cloud.GoogleSecretProvider
import org.fog_rock.frlineagent.infrastructure.internal.mock.MockSecretProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SecretManagerProviderTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInit_cloudMode() {
        val appConfig = mockk<AppConfig>()
        every { appConfig.secretManagerMode } returns ProviderMode.CLOUD
        every { appConfig.googleCloudProjectId } returns "test-project"

        mockkConstructor(GoogleSecretProvider::class)
        every { anyConstructed<GoogleSecretProvider>().getSecret("test-key") } returns "cloud-secret"

        val secretManagerProvider = SecretManagerProvider(appConfig)
        val result = secretManagerProvider.getSecret("test-key")

        assertEquals("cloud-secret", result)

        // Verify that GoogleSecretProvider was instantiated
        verify(exactly = 1) { anyConstructed<GoogleSecretProvider>().getSecret("test-key") }
    }

    @Test
    fun testInit_mockMode() {
        val appConfig = mockk<AppConfig>()
        every { appConfig.secretManagerMode } returns ProviderMode.MOCK

        mockkConstructor(MockSecretProvider::class)
        every { anyConstructed<MockSecretProvider>().getSecret("test-key") } returns "mock-secret"

        val secretManagerProvider = SecretManagerProvider(appConfig)
        val result = secretManagerProvider.getSecret("test-key")

        assertEquals("mock-secret", result)

        // Verify that MockSecretProvider was instantiated
        verify(exactly = 1) { anyConstructed<MockSecretProvider>().getSecret("test-key") }
    }
}
