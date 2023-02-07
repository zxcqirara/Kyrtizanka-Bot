package bot.lib.discord_tokens.dto

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlags
import dev.kord.common.entity.UserPremium
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class AccountObject {
	abstract val id: Snowflake
	abstract val username: String
	@SerialName("display_name")
	abstract val displayName: String?
	abstract val avatar: String?
	@SerialName("avatar_decoration")
	abstract val avatarDecoration: String?
	abstract val discriminator: String
	@SerialName("public_flags")
	abstract val publicFlags: UserFlags
	@SerialName("flags")
	abstract val flags: UserFlags
	abstract val banner: String?
	@SerialName("banner_color")
	abstract val bannerColor: String?
	@SerialName("accent_color")
	abstract val accentColor: Int?
	abstract val bio: String
	abstract val locale: Locale
	@SerialName("mfa_enabled")
	abstract val mfaEnabled: Boolean
	@SerialName("premium_type")
	abstract val premiumType: UserPremium
	abstract val email: String?
	abstract val verified: Boolean

	val fullName get() = "$username#$discriminator"
}