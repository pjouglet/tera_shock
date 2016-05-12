package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.MultiShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель ссылки на мульти магазин.
 *
 * @author Ronn
 */
public final class ReplyMultiShop extends AbstractReply
{
	/** массив итемов */
	private ItemTemplate[] items;

	/** массив цен */
	private int[] price;

	/** ид итема, который выступает как цена */
	private int priceId;

	public ReplyMultiShop(Node node)
	{
		super(node);

		VarTable vars = VarTable.newInstance(node);

		// получаем ид секции
		priceId = vars.getInteger("priceId");

		// подготавливаем список итемов
		Array<ItemTemplate> itemList = Arrays.toArray(ItemTemplate.class);
		// подготавливаем список цен
		Array<Integer> priceList = Arrays.toArray(Integer.class);

		// перебираем внутренние элементы
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// если это описание итема
			if("item".equals(child.getNodeName()))
			{
				// парсим атрибуты
				vars.parse(child);

				// получаем ид итема
				int id = vars.getInteger("id");

				// получаем таблицу итемов
				ItemTable itemTable = ItemTable.getInstance();

				// получаем темплейт итема
				ItemTemplate template = itemTable.getItem(id);

				// если его нет, пропускаем
				if(template == null)
				{
					log.warning("not itemId " + id + " in item table.");
					continue;
				}

				int price = vars.getInteger("price");

				// добавляем в список
				itemList.add(template);
				priceList.add(price);
			}
		}

		// создаем массив итемов
		items = new ItemTemplate[itemList.size()];
		// создаем массив цен
		price = new int[priceList.size()];

		for(int i = 0, length = itemList.size(); i < length; i++)
		{
			items[i] = itemList.get(i);
			price[i] = priceList.get(i);
		}
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// создаем диалог магазина
		Dialog dialog = MultiShopDialog.newInstance(npc, player, items, price, priceId);

		// если неудачно инициализировался, закрываем
		if(!dialog.init())
			dialog.close();
	}
}
