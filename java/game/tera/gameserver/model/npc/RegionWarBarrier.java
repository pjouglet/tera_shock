package tera.gameserver.model.npc;

import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWarNpc;
import tera.gameserver.model.Guild;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель забора в осадах регионов.
 *
 * @author Ronn
 */
public class RegionWarBarrier extends NpcBarrier implements RegionWarNpc
{
	public RegionWarBarrier(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void setGuildOwner(Guild guild){}

	@Override
	public void setRegion(Region region){}

	@Override
	public Region getRegion()
	{
		return null;
	}

	@Override
	public Guild getGuildOwner()
	{
		return null;
	}
}
