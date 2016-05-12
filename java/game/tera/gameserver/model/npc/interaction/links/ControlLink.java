package tera.gameserver.model.npc.interaction.links;

import tera.gameserver.model.Guild;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.npc.spawn.RegionWarSpawn;
import tera.gameserver.model.playable.Player;

/**
 * Модель линка телепорта к захваченной точке.
 *
 * @author Ronn
 */
public class ControlLink extends AbstractLink
{
	/** спавн целевого контрола */
	private RegionWarSpawn spawn;

	public ControlLink(String name, RegionWarSpawn spawn, Reply reply)
	{
		super(name, LinkType.DIALOG, IconType.DIALOG, reply, null);

		this.spawn = spawn;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		// получаем владельца точки
		Guild owner = spawn.getOwner();

		// есмли его нет, значит нельзя туда ТП
		if(owner == null)
			return false;

		// является ли глидьия игрока владеющей
		return owner == player.getGuild();
	}

	/**
	 * @return спавн ссылки.
	 */
	public RegionWarSpawn getSpawn()
	{
		return spawn;
	}

	@Override
	public String toString()
	{
		return "ControlLink  name = " + name + ", type = " + type + ", icon = " + icon;
	}
}
