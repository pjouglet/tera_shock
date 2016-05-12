package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Arrays;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.TaxationNpc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.SkillShopDialog;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки на магазин скилов.
 *
 * @author Ronn
 */
public final class ReplySkillShop extends AbstractReply
{
	/** доступные классы */
	private PlayerClass[] available;

	public ReplySkillShop(Node node)
	{
		super(node);

		VarTable vars = VarTable.newInstance(node);

		this.available = vars.getEnumArray("classes", PlayerClass.class, ",");
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// если не подходящий класс игрока, выходим
		if(!Arrays.contains(available, player.getPlayerClass()))
		{
			player.sendMessage("You don't have the appropriate class");
			return;
		}

		// ссылка на банк для отчилсений
		Bank bank = null;

		// итоговый налог
		float resultTax = 1;

		// если НПС имеет налог
		if(npc instanceof TaxationNpc)
		{
			TaxationNpc taxation = (TaxationNpc) npc;

			// получаем банк для отчисления
			bank = taxation.getTaxBank();

			// рассчитываем итоговый налог
			resultTax = 1 + (taxation.getTax() / 100F);
		}

		// создаем новый диалог шопа
		Dialog dialog = SkillShopDialog.newInstance(npc, player, bank, resultTax);

		// если его не удалось инициализировать
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
