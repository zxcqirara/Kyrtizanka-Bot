package bot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.selfMember
import dev.kord.core.event.message.MessageCreateEvent

class Tuts : Extension() {
	override val name = "tuts"

	override suspend fun setup() {
		event<MessageCreateEvent> {
			check { failIf(event.member == event.getGuild()?.selfMember()) }

			action {
				if (event.message.content.contains("туц", true))
					event.message.channel.createMessage("Туц")
			}
		}
	}
}