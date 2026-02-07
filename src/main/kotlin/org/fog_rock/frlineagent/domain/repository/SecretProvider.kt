package org.fog_rock.frlineagent.domain.repository

/**
 * An interface for providing secret values.
 */
interface SecretProvider {
    /**
     * Get a secret value.
     * @param key The key of the secret.
     * @return The secret value.
     */
    fun getSecret(key: String): String
}
