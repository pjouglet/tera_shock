package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.LoadGuildIcon;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки на диалог загрузки иконки гильдии.
 * 
 * @author Ronn
 */
public final class ReplyLoagGuildIcon extends AbstractReply
{
	/**
	 * @param node данные с хмл.
	 */
	public ReplyLoagGuildIcon(Node node)
	{
		super(node);
	}
	
	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// получаем гильдию игрока
		Guild guild = player.getGuild();
		
		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}
		
		// получаем ранг игрока в гильдии
		GuildRank rank = player.getGuildRank();
		
		// если это не лидер
		if(!rank.isGuildMaster())
		{
			// сообщаем и выходим
			player.sendMessage(MessageType.YOU_ARE_NOT_THE_GUILD_MASTER);
			return;
		}
		
		// создаем новый диалог
		Dialog dialog = LoadGuildIcon.newInstance(npc, player);
		
		// если неудачно инициализировался
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
