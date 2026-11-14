package dev.jakubzika.befair.data.db

import dev.jakubzika.befair.domain.model.UserSettingsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

/**
 * Data-access layer for [Users]. Each call runs a blocking Exposed transaction on the IO
 * dispatcher so route handlers can stay `suspend`.
 */
class UserRepository {

    suspend fun findByEmail(email: String): UserRow? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map(::toUserRow)
            .singleOrNull()
    }

    suspend fun findById(id: Int): UserRow? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map(::toUserRow)
            .singleOrNull()
    }

    /**
     * Inserts a new, unverified user together with its default settings (see [UserSettingsDto])
     * in one transaction, and returns the generated id.
     */
    suspend fun create(email: String, displayName: String, passwordHash: String): Int = dbQuery {
        val userId = Users.insert {
            it[Users.email] = email
            it[Users.displayName] = displayName
            it[Users.passwordHash] = passwordHash
            it[Users.isVerified] = false
        }[Users.id]
        val defaults = UserSettingsDto()
        UserSettings.insert {
            it[UserSettings.userId] = userId
            it[currency] = defaults.currency.name
            it[customCurrency] = defaults.customCurrency
            it[toolBasis] = defaults.toolBasis.name
            it[updatedAt] = System.currentTimeMillis()
        }
        userId
    }

    suspend fun updatePasswordHash(email: String, passwordHash: String) {
        dbQuery {
            Users.update({ Users.email eq email }) {
                it[Users.passwordHash] = passwordHash
            }
        }
    }

    suspend fun setOtp(email: String, otp: String, expiresAt: Long) {
        dbQuery {
            Users.update({ Users.email eq email }) {
                it[otpCode] = otp
                it[otpExpiresAt] = expiresAt
            }
        }
    }

    /** Marks the user verified and clears the consumed OTP in a single update. */
    suspend fun markVerifiedAndClearOtp(email: String) {
        dbQuery {
            Users.update({ Users.email eq email }) {
                it[isVerified] = true
                it[otpCode] = null
                it[otpExpiresAt] = null
            }
        }
    }

    suspend fun deleteByEmail(email: String) {
        dbQuery {
            Users.deleteWhere { Users.email eq email }
        }
    }

    private fun toUserRow(row: ResultRow) = UserRow(
        id = row[Users.id],
        email = row[Users.email],
        passwordHash = row[Users.passwordHash],
        displayName = row[Users.displayName],
        isVerified = row[Users.isVerified],
        otpCode = row[Users.otpCode],
        otpExpiresAt = row[Users.otpExpiresAt],
    )

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }
}
