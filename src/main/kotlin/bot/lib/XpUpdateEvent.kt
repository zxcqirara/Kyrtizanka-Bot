package bot.lib

import com.kotlindiscord.kord.extensions.events.KordExEvent

class XpUpdateEvent(
	newXp: Long,
	oldLevel: Int
) : KordExEvent {
	var newLevel = Utils.levelOfXp(newXp)
	var needUpdate = oldLevel < newLevel
}