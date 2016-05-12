package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.CreateGuildDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Модель ответа на ссылку создания гильдии.
 * 
 * @author Ronn
 */
public final class ReplyCreateGuild extends AbstractReply
{
	/** стоимость создания клана */
	private int price;
	/** минимальный уровень */
	private int minLevel;
	
	/**
	 * @param node данные с хмл.
	 */
	public ReplyCreateGuild(Node node)
	{
		super(node);
		
		VarTable vars = VarTable.newInstance(node);
		
		price = vars.getInteger("price");
		minLevel = vars.getInteger("minLevel");
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// если игрок меньше нужного уровня, выходим
		if(player.getLevel() < minLevel)
		{
			player.sendMessage("You must be level 8 for create a Guild.");
			return;
		}
		
		// создаем диалог создания гильдии
		Dialog dialog = CreateGuildDialog.newInstance(npc, player, price, minLevel);
		
		// если он неудачно инициализировался
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
