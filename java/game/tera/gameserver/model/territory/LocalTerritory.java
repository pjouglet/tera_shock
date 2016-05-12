package tera.gameserver.model.territory;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.util.Location;

/**
 * Модель территории локального региона.
 *
 * @author Ronn
 */
public final class LocalTerritory extends AbstractTerritory
{
	/** очка для телепорта */
	private Location teleportLoc;

	public LocalTerritory(Node node, TerritoryType type)
	{
		super(node, type);

		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		// создаем точку для телепорта в регион
		this.teleportLoc = new Location(vars.getFloat("x"), vars.getFloat("y"), vars.getFloat("z"), 0, getContinentId());
	}

	/**
	 * Точка для телепорта.
	 */
	public Location getTeleportLoc()
	{
		return teleportLoc;
	}

	@Override
	public void onEnter(TObject object)
	{
		super.onEnter(object);

		// если вошедший объект игрок
		if(object.isPlayer())
		{
			// получаем игрока
			Player player = object.getPlayer();

			// если это зона им неизведана
			if(!player.isWhetherIn(this))
			{
				// синхронизируемся
				synchronized(this)
				{
					// если точно не изведана
					if(!player.isWhetherIn(this))
					{
						// заносим ее в обнаруженные с сохранением в БД
						player.storeTerritory(this, true);
						// уведомляем об этом
						player.sendPacket(SystemMessage.getInstance(MessageType.DISCOVERED_SECTION_NAME).add("sectionName",  name), true);
					}
				}
			}
		}
	}
}
