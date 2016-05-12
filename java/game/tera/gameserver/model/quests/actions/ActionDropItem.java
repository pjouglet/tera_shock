package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.Rnd;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.LocalObjects;

/**
 * Акшен для реализации дропа квест. итема.
 *
 * @author Ronn
 */
public class ActionDropItem extends AbstractQuestAction
{
	/** ид итема */
	private int id;
	/** мин. кол-во */
	private int min;
	/** макс. кол-во */
	private int max;
	/** шанс */
	private int chance;

	public ActionDropItem(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.id = vars.getInteger("id");
			this.min = vars.getInteger("min");
			this.max = vars.getInteger("max");
			this.chance = vars.getInteger("chance");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем нпс
		Npc npc = event.getNpc();

		// если нет нпс, выходим
		if(npc == null)
		{
			log.warning(this, "not found npc");
			return;
		}

		// проверяем отношение итема к донату
		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, id))
		{
			log.warning(this, new Exception("not create donate item for id " + id));
			return;
		}

		// если не сработал шанс, выходим
		if(!Rnd.chance(chance))
			return;

		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		// получаем темплейт итема
		ItemTemplate template = itemTable.getItem(id);

		// если темплейта нет, выходим
		if(template == null)
		{
			log.warning(this, "not found item template");
			return;
		}

		// получаем итем
		ItemInstance item = template.newInstance();

		//если итем стакуемый
		if(item.isStackable())
			// расчитываем его кол-во
			item.setItemCount(Math.max(Rnd.nextInt(min, max), 1));

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем список итемов
		Array<ItemInstance> items = local.getNextItemList();

		// добавляем в него итем
		items.add(item);

		// дропаем вокруг нпс
		Npc.spawnDropItems(npc, items.array(), items.size());
	}

	@Override
	public String toString()
	{
		return "ActionDropItem id = " + id + ", min = " + min + ", max = " + max + ", chance = " + chance;
	}
}
