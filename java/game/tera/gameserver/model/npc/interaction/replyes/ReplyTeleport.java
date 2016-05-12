package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.model.TeleportRegion;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.TeleportDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.LocalTerritory;
import tera.gameserver.tables.TerritoryTable;


/**
 * Модель ответа на ссылку телепорта.
 *
 * @author Ronn
 */
public class ReplyTeleport extends AbstractReply
{
	/** таблица доступных телепортов */
	private Table<IntKey, TeleportRegion> table;

	/** доступные регионы */
	private TeleportRegion[] regions;

	public ReplyTeleport(Node node)
	{
		super(node);

		// получаем таблицу территорий
		TerritoryTable territoryTable = TerritoryTable.getInstance();

		// перебираем узлы
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			// если неподходящий, пропускаем
			if(child.getNodeType() != Node.ELEMENT_NODE || !"loc".equals(child.getNodeName()))
				continue;

			VarTable vars = VarTable.newInstance(child);

			// получаем нужну. территорию
			LocalTerritory region = (LocalTerritory) territoryTable.getTerritory(vars.getString("name"));

			if(region == null)
			{
				log.warning(this, "not found territory for name " + vars.getString("name"));
				continue;
			}

			// получаем цену телепорта
			int price = vars.getInteger("price");

			// индекс точки
			int index = vars.getInteger("index");

			// добавляем в список точку
			regions = Arrays.addToArray(regions, new TeleportRegion(region, price, index), TeleportRegion.class);
		}

		// созадем таблицу телепортов
		table = Tables.newIntegerTable();

		// заполняем таблицу
		for(TeleportRegion region : regions)
		{
			if(table.containsKey(region.getIndex()))
				log.warning(this, new Exception("found duplicate teleport region for index " + region.getIndex()));

			table.put(region.getIndex(), region);
		}
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		if(regions == null || regions.length < 1)
			return;

		// создаем диалог
		TeleportDialog dialog = TeleportDialog.newInstance(npc, player, regions, table);

		// если он неудачно инициализировался
		if(!dialog.init())
			// закрываем
			dialog.close();
	}
}
