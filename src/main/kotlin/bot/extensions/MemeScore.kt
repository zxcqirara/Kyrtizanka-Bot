package bot.extensions

import bot.lib.Database
import bot.readConfig
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import kotlinx.coroutines.flow.count
import kotlinx.datetime.Clock

class MemeScore : Extension() {
	override val name = "meme-score"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		event<ReactionAddEvent> {
			check {
				failIf(event.message.channelId != Snowflake(readConfig().memesChannelId))
				failIf(event.getUser() == event.message.asMessage().author)
				isNotBot()
				failIf(event.emoji.name != "👍")
			}

			action {
				if (event.getUser().isBot) return@action

				val count = event.message.getReactors(event.emoji).count()
				val maxScore = Database.memeMaxScore(event.messageId)
					?: run { Database.createMeme(event.messageId); 0 }

				if (count <= maxScore) return@action

				val userId = event.userId.value.toLong()
				val score = readConfig().memeScore * count

				Database.setMemeScore(event.messageId, count)

				val xpEvent = Database.addExperience(
					bot, userId, score.toShort(), Clock.System.now()
				)

				if (xpEvent.needUpdate) {
					event.message.channel.createEmbed {
						title = translate("extensions.experience.newLevel")
						description = translate(
							"extensions.experience.reachedLevel",
							arrayOf(event.user.mention, xpEvent.newLevel)
						)
					}
				}
			}
		}

		event<MessageCreateEvent> {
			check {
				failIf(event.message.channelId != Snowflake(readConfig().memesChannelId))
				isNotBot()
				failIf(event.member?.isBot ?: true)
			}

			action {
				Database.createMeme(event.message.id)
			}
		}
	}
}