package bot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.edit
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent

class AutoRegion : Extension() {
	override val name = "auto-region"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		event<VoiceStateUpdateEvent> {
			action {
				val channel = event.state.getChannelOrNull() ?: return@action

				channel.asChannelOf<VoiceChannel>().edit {
					rtcRegion = "russia"
				}
			}
		}
	}
}