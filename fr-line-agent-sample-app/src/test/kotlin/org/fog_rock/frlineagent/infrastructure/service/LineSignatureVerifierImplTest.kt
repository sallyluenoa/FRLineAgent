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
import org.fog_rock.frlineagent.infrastructure.internal.cloud.LineSignatureCloudVerifier
import org.fog_rock.frlineagent.infrastructure.internal.mock.LineSignatureMockVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LineSignatureVerifierImplTest {

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
    fun testVerify_cloudMode() {
        every { appConfig.lineApiMode } returns ProviderMode.CLOUD
        every { appConfig.lineBotChannelSecretKey } returns "test-secret-key"
        every { secretProvider.getSecret("test-secret-key") } returns "test-secret"

        mockkConstructor(LineSignatureCloudVerifier::class)
        every { anyConstructed<LineSignatureCloudVerifier>().verify(any(), any()) } returns true

        val verifier = LineSignatureVerifierImpl(appConfig, secretProvider)
        val result = verifier.verify("body", "signature")

        assertTrue(result)
        verify { anyConstructed<LineSignatureCloudVerifier>().verify("body", "signature") }
    }

    @Test
    fun testVerify_mockMode() {
        every { appConfig.lineApiMode } returns ProviderMode.MOCK

        mockkConstructor(LineSignatureMockVerifier::class)
        every { anyConstructed<LineSignatureMockVerifier>().verify(any(), any()) } returns true

        val verifier = LineSignatureVerifierImpl(appConfig, secretProvider)
        val result = verifier.verify("body", "signature")

        assertTrue(result)
        verify { anyConstructed<LineSignatureMockVerifier>().verify("body", "signature") }
    }
}
