package bot.extensions

import bot.lib.Config
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.channel.edit
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Guild
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class PrivateRooms : Extension() {
	override val name = "private-rooms"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		val config = Config.discord

		val rooms = indexRooms(
			kord.getGuild(Snowflake(config.guildId)),
			Snowflake(config.privates.categoryId),
			Snowflake(config.privates.createChannelId)
		).toMutableList()

		event<VoiceStateUpdateEvent> {
			action {
				if (event.state.channelId == Snowflake(Config.discord.privates.createChannelId)) {
					val member = event.state.getMember()
					val name = "🔐 ${member.nickname}"

					val channel = (event.state.getChannelOrNull() as VoiceChannel).category?.createVoiceChannel(name)
						?: return@action
					channel.addOverwrite(PermissionOverwrite.forMember(member.id, Permissions(Permission.ManageChannels)))

					member.edit { voiceChannelId = channel.id }

					rooms += channel.id
				}
				if (event.old?.channelId != null && rooms.contains(event.old!!.channelId)) {
					val oldState = event.old!!
					val oldChannel = oldState.getChannelOrNull()!! as VoiceChannel

					val allowed = oldChannel.permissionOverwrites
						.filter { it.allowed.contains(Permission.ManageChannels) }
						.map { it.target }

					if (oldChannel.voiceStates.toList().isEmpty()) {
						oldChannel.delete()

						rooms -= oldState.channelId!!
					} else if (allowed.contains(oldState.channelId)) {
						val next = oldChannel.voiceStates
							.map { it.getMember() }
							.filter { !it.getVoiceState().isMuted && !it.getVoiceState().isSelfMuted }
							.filter { !it.getVoiceState().isDeafened && !it.getVoiceState().isSelfDeafened }
							.toList().random()

						oldChannel.addOverwrite(
							PermissionOverwrite.forMember(next.id, Permissions(Permission.ManageChannels))
						)

						oldChannel.edit { name = "🔐 ${next.nickname}" }
					}
				}
			}
		}
	}

	private suspend fun indexRooms(
		guild: Guild,
		categoryId: Snowflake,
		createChannelId: Snowflake
	): List<Snowflake> {
		val category = guild.getChannelOf<Category>(categoryId)
		val createChannel = guild.getChannelOf<VoiceChannel>(createChannelId)

		return category.channels
			.filter { it.type == ChannelType.GuildVoice }
			.filter { it != createChannel }
			.map { it.id }
			.toList()
	}
}