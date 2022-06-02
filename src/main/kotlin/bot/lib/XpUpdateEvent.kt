package bot.lib

import com.kotlindiscord.kord.extensions.events.KordExEvent

class XpUpdateEvent(
	newXp: Long,
	oldLevel: Int
) : KordExEvent {
	var newLevel: Int
	var needUpdate: Boolean

	init {
		newLevel = Utils.levelOfXp(newXp)

		needUpdate = oldLevel < newLevel
	}
}