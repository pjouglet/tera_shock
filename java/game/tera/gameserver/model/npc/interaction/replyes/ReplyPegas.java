package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.model.Route;
import tera.gameserver.model.TownInfo;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.PegasDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.tables.TownTable;


/**
 * Модель ответа на ссылку перелетов на пегасе.
 *
 * @author Ronn
 */
public final class ReplyPegas extends AbstractReply
{
	/** маршруты полета полета */
	private Table<IntKey, Route> routes;

	/** город вылета */
	private TownInfo town;

	public ReplyPegas(Node node)
	{
		super(node);

		this.routes = Tables.newIntegerTable();

		// получаем таблицу городов
		TownTable townTable = TownTable.getInstance();

		this.town = townTable.getTown(node.getAttributes().getNamedItem("town").getNodeValue());

		Array<Route> list = Arrays.toArray(Route.class);

		for(Node route = node.getFirstChild(); route != null; route = route.getNextSibling())
		{
			if("route".equals(route.getNodeName()))
			{
				// парсим атрибуты
				VarTable vars = VarTable.newInstance(route);

				// получаем индекс маршрута
				int index = vars.getInteger("index");

				// получаем цену маршрута
				int price = vars.getInteger("price");

				// получаем целевой город
				TownInfo target = townTable.getTown(vars.getString("target"));

				// добавляем новый маршрут
				list.add(new Route(index, price, target, vars.getBoolean("short", false)));
			}
		}

		// размещаем маршруты в таблице
		for(Route route : list)
			routes.put(route.getIndex(), route);
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// создаем новое окно
		PegasDialog window = PegasDialog.newInstance(npc, player, routes, town);

		// если не удалось инициализировать
		if(!window.init())
			// закрываем
			window.close();
	}
}
