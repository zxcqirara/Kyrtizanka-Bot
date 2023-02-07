package bot.lib.discord_tokens.dto

import dev.kord.common.entity.GuildFeature
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
class GuildObject(
	val id: Snowflake,
	val name: String,
	val owner: Boolean
)