package bot.lib.discord_tokens.dto

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlags
import dev.kord.common.entity.UserPremium
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccountObject(
	override val id: Snowflake,
	override val username: String,
	@SerialName("display_name")
	override val displayName: String?,
	override val avatar: String?,
	@SerialName("avatar_decoration")
	override val avatarDecoration: String?,
	override val discriminator: String,
	@SerialName("public_flags")
	override val publicFlags: UserFlags,
	override val flags: UserFlags,
	override val banner: String?,
	@SerialName("banner_color")
	override val bannerColor: String?,
	@SerialName("accent_color")
	override val accentColor: Int?,
	override val bio: String,
	override val locale: Locale,
	@SerialName("mfa_enabled")
	override val mfaEnabled: Boolean,
	@SerialName("premium_type")
	override val premiumType: UserPremium,
	override val email: String?,
	override val verified: Boolean,

	@SerialName("purchased_flags")
	val purchasedFlags: Int = 0,
	@SerialName("premium_usage_flags")
	val premiumUsageFlags: Int = 0,
	@SerialName("nsfw_allowed")
	val nsfwAllowed: Boolean,
	val phone: String?
) : AccountObject()