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

package org.fog_rock.frlineagent.infrastructure.internal.cloud

import com.linecorp.bot.parser.LineSignatureValidator
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.fog_rock.frlineagent.domain.config.AppConfig
import org.fog_rock.frlineagent.domain.repository.SecretProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LineSignatureCloudVerifierTest {

    private lateinit var appConfig: AppConfig
    private lateinit var secretProvider: SecretProvider
    private lateinit var verifier: LineSignatureCloudVerifier

    @BeforeEach
    fun setUp() {
        appConfig = mockk()
        secretProvider = mockk()

        every { appConfig.lineBotChannelSecretKey } returns "test-secret-key"
        every { secretProvider.getSecret("test-secret-key") } returns "test-secret"

        mockkConstructor(LineSignatureValidator::class)

        verifier = LineSignatureCloudVerifier(appConfig, secretProvider)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testVerify_success() {
        val body = "test-body"
        val signature = "test-signature"

        every { anyConstructed<LineSignatureValidator>().validateSignature(any(), any()) } returns true

        val result = verifier.verify(body, signature)

        assertTrue(result)
        verify { anyConstructed<LineSignatureValidator>().validateSignature(any(), any()) }
    }

    @Test
    fun testVerify_failure() {
        val body = "test-body"
        val signature = "invalid-signature"

        every { anyConstructed<LineSignatureValidator>().validateSignature(any(), any()) } returns false

        val result = verifier.verify(body, signature)

        assertFalse(result)
    }

    @Test
    fun testVerify_exception() {
        val body = "test-body"
        val signature = "test-signature"

        every { anyConstructed<LineSignatureValidator>().validateSignature(any(), any()) } throws RuntimeException("Validation Error")

        val result = verifier.verify(body, signature)

        assertFalse(result)
    }
}
