package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.PlayerBankDialog;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки на банк игрока.
 * 
 * @author Ronn
 */
public final class ReplyPlayerBank extends AbstractReply
{
	public ReplyPlayerBank(Node node)
	{
		super(node);
	}
	
	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// создаем новый диалог
		Dialog dialog = PlayerBankDialog.newInstance(npc, player);
		
		// если его не удалось инициализировать
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
