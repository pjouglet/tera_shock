package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.TaxationNpc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель ссылки на обычный магазин.
 *
 * @author Ronn
 */
public final class ReplyShop extends AbstractReply
{
	/** массив секций с итемами */
	private ItemTemplate[][] sections;

	/** все итемы доступные на продажу */
	private Table<IntKey, ItemTemplate> availableItems;

	/** ид первой секции */
	private int sectionId;

	public ReplyShop(Node node)
	{
		super(node);

		VarTable vars = VarTable.newInstance(node);

		// получаем ид секции
		sectionId = vars.getInteger("sectionId");

		// подготавливаем таблицу секций
		Array<Array<ItemTemplate>> sectionList = Arrays.toArray(Array.class);

		// получаем таблицу скилов
		ItemTable itemTable = ItemTable.getInstance();

		// перебираем внутренние элементы
		for(Node section = node.getFirstChild(); section != null; section = section.getNextSibling())
		{
			if(section.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// если это секция
			if("section".equals(section.getNodeName()))
			{
				Array<ItemTemplate> items = Arrays.toArray(ItemTemplate.class);

				// перебираем итемы входящие в секцию
				for(Node item = section.getFirstChild(); item != null; item = item.getNextSibling())
				{
					if(item.getNodeType() != Node.ELEMENT_NODE)
						continue;

					// если это описание итема
					if("item".equals(item.getNodeName()))
					{
						// парсим атрибуты
						vars.parse(item);

						// получаем ид итема
						int id = vars.getInteger("id");

						// получаем темплейт итема
						ItemTemplate template = itemTable.getItem(id);

						// если его нет, пропускаем
						if(template == null)
						{
							log.warning("not itemId " + id + " in item table.");
							continue;
						}

						// добавляем в список
						items.add(template);
					}
				}

				// одбавляем новую секцию
				sectionList.add(items);
			}
		}

		// создаем таблицу секций
		sections = new ItemTemplate[sectionList.size()][];

		// получаем список секций
		Array<ItemTemplate>[] array = sectionList.array();

		// заполняем таблицу секций
		for(int i = 0, length = sectionList.size(); i < length; i++)
			sections[i] = array[i].trimToSize().array();

		// счетчик всех итемов
		int counter = 0;

		// подсчитываем общее кол-во итемов
		for(ItemTemplate[] items : sections)
			counter += items.length;

		// если нет не одного, прерываемся
		if(counter < 1)
			throw new IllegalArgumentException("no items");

		// создаем таблицу доступных итемов
		availableItems = Tables.newIntegerTable();

		// заполняем таблицу
		for(ItemTemplate[] items : sections)
			for(ItemTemplate item : items)
				availableItems.put(item.getItemId(), item);
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
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

		// создаем диалог магазина
		Dialog dialog = ShopDialog.newInstance(npc, sections, availableItems, player, bank, sectionId, resultTax);

		// если неудачно инициализировался, закрываем
		if(!dialog.init())
			dialog.close();
	}
}
