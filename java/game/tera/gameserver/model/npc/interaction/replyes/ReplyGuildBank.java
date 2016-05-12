package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.model.Guild;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.GuildBankDialog;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки на банк гильдии.
 * 
 * @author Ronn
 */
public final class ReplyGuildBank extends AbstractReply
{
	public ReplyGuildBank(Node node)
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
		
		// создаем новый диалог
		Dialog dialog = GuildBankDialog.newInstance(npc, player);
		
		// если инициализация не удачна
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
