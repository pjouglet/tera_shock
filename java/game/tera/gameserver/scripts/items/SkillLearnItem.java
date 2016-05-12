package tera.gameserver.scripts.items;

import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель итемов, изучающие скилы.
 *
 * @author Ronn
 */
public class SkillLearnItem extends AbstractItemExecutor
{
	/** таблица изучаемых скилов */
	private final Table<IntKey, SkillTemplate[]> skillTable;

	public SkillLearnItem(int[] itemIds, int access)
	{
		super(itemIds, access);

		this.skillTable = Tables.newIntegerTable();

		try
		{
			skillTableInit();
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void execution(ItemInstance item, Player player)
	{
		// получаем темплейт изучаемого скила
		SkillTemplate[] template = skillTable.get(item.getItemId());

		// если темплейтов нет, ыходим
		if(template == null || template.length < 1)
			return;

		// получаем первый темплейт в массиве
		SkillTemplate first = template[0];

		// если скил такой уже есть, выходим
		if(player.getSkill(first.getId()) != null)
			return;

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если такой итем удалился из него
		if(inventory != null && inventory.removeItem(item.getItemId(), 1L))
		{
			// изучаем скил
			player.addSkills(template, true);

			// отпровляем сообщение об изучении
			player.sendPacket(SystemMessage.getInstance(MessageType.YOUVE_LEARNED_SKILL_NAME).addSkillName(template[0].getName()), true);

			// отправляем пакет о использовании рецепта
			player.sendPacket(SystemMessage.getInstance(MessageType.ITEM_USE).addItem(item.getItemId(), 1), true);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// обновляем инвентарь
			eventManager.notifyInventoryChanged(player);
		}
	}

	/**
	 * Инициализация таблицы скилов.
	 */
	private void skillTableInit()
	{
		// Get a table skill
		SkillTable table = SkillTable.getInstance();

		//book for the study of the first mount
		skillTable.put(20, table.getSkills(-15, 67219975));

		//book for the study of the another mounts/book for the study of the another mounts
		skillTable.put(21, table.getSkills(-15, 67219976));
		skillTable.put(41, table.getSkills(-15, 67219978));
		skillTable.put(166, table.getSkills(-15, 67219991));
		skillTable.put(167, table.getSkills(-15, 67219980));
		skillTable.put(168, table.getSkills(-15, 67219981));
		skillTable.put(169, table.getSkills(-15, 67219982));
		skillTable.put(170, table.getSkills(-15, 67219983));
		skillTable.put(306, table.getSkills(-15, 67219985));
		skillTable.put(307, table.getSkills(-15, 67219986));
		skillTable.put(336, table.getSkills(-15, 67220054));
		skillTable.put(350, table.getSkills(-15, 67219988));
		skillTable.put(351, table.getSkills(-15, 67219989));
		skillTable.put(384, table.getSkills(-15, 67220061));
		skillTable.put(385, table.getSkills(-15, 67220062));
		skillTable.put(412, table.getSkills(-15, 67219990));
		skillTable.put(413, table.getSkills(-15, 67219991));
		skillTable.put(414, table.getSkills(-15, 67219992));
		skillTable.put(415, table.getSkills(-15, 67219981));
		skillTable.put(416, table.getSkills(-15, 67219982));
		skillTable.put(417, table.getSkills(-15, 67219996));
		skillTable.put(425, table.getSkills(-15, 67220056));
	}
}
