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

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.protobuf.ByteString
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GoogleSecretProviderTest {

    private val projectId = "test-project-id"
    private lateinit var googleSecretProvider: GoogleSecretProvider
    private lateinit var mockClient: SecretManagerServiceClient

    @BeforeEach
    fun setUp() {
        googleSecretProvider = GoogleSecretProvider(projectId)
        mockClient = mockk(relaxed = true)

        mockkStatic(SecretManagerServiceClient::class)
        every { SecretManagerServiceClient.create() } returns mockClient
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testGetSecret_success() {
        val key = "test-key"
        val secretValue = "test-secret-value"
        val secretVersionName = SecretVersionName.of(projectId, key, "latest")

        val mockResponse = mockk<AccessSecretVersionResponse>()
        val mockPayload = mockk<SecretPayload>()
        val mockByteString = mockk<ByteString>()

        every { mockClient.accessSecretVersion(any<SecretVersionName>()) } returns mockResponse
        every { mockResponse.payload } returns mockPayload
        every { mockPayload.data } returns mockByteString
        every { mockByteString.toStringUtf8() } returns secretValue

        val result = googleSecretProvider.getSecret(key)

        assertEquals(secretValue, result)
        verify {
            mockClient.accessSecretVersion(match<SecretVersionName> {
                it.project == projectId && it.secret == key && it.secretVersion == "latest"
            })
            mockClient.close()
        }
    }

    @Test
    fun testGetSecret_projectIdNotSet() {
        val provider = GoogleSecretProvider("")
        assertThrows<IllegalStateException> {
            provider.getSecret("some-key")
        }
    }

    @Test
    fun testGetSecret_sdkException() {
        val key = "test-key"
        every { mockClient.accessSecretVersion(any<SecretVersionName>()) } throws RuntimeException("SDK Error")

        assertThrows<RuntimeException> {
            googleSecretProvider.getSecret(key)
        }
        verify { mockClient.close() }
    }
}
