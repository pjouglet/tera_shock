package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.Tp1;
import tera.gameserver.network.serverpackets.WorldZone;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.Location;

/**
 * Клиентский пакет с уведомлением о согласии лететь на респ после смерти.
 *
 * @author Ronn
 */
public class RequestRessurect extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		readInt();
		readInt();
	}

	@Override
	public void runImpl()
	{
		// если нельзя воскресится, выходим
		if(player == null || !player.isResurrected())
			return;

		// получаем сумон
		Summon summon = player.getSummon();

		// если сумон есть
		if(summon != null)
			// удаляем его
			summon.remove();

		// удаляем игрока из текущей области
		player.decayMe(DeleteCharacter.DISAPPEARS);

		// получаем таблицу зон
		WorldZoneTable zoneTable = WorldZoneTable.getInstance();

		// получаем точку воскрешения
		Location point = zoneTable.getRespawn(player);

		// если не нашли, выходим
		if(point == null)
		{
			log.warning(this, "not found respawn for " + player.getLoc());
			return;
		}

		// меняем позицию
		player.setLoc(point);

		// получаем ид новой зоны
		int zoneId = World.getRegion(player).getZoneId(player);

		// если ид зоны не известен
		if(zoneId < 1)
			// берем дефолтный
			zoneId = player.getContinentId() + 1;

		// применяем ид зоны
		player.setZoneId(zoneId);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем ид зоны
		eventManager.notifyChangedZoneId(player);

		// если игрок был убит не на боевой зоне
		if(!player.isInBattleTerritory())
			// обнуляем стамину
			player.setStamina(0);

		// восстанавливаем хп/мп
		player.setCurrentHp(player.getMaxHp() / 3);
		player.setCurrentMp(player.getMaxMp() / 3);

		// прогружаемся
		player.sendPacket(Tp1.getInstance(player), true);
		player.sendPacket(WorldZone.getInstance(player), true);
	}
}