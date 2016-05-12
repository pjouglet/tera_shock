package tera.gameserver.model.npc;

import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWarNpc;
import tera.gameserver.model.Guild;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель НПС, который работает с захватиываемым регионом.
 * 
 * @author Ronn
 */
public class RegionWarShop extends FriendNpc implements TaxationNpc, RegionWarNpc
{
	/** владелющий регион */
	private Region region;

	public RegionWarShop(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getTax()
	{
		Region region = getRegion();

		if(region == null)
		{
			log.warning("not found region for NPC " + getTemplateId() + " - " + getTemplateType());
			return 0;
		}

		Guild owner = region.getOwner();
		return owner == null ? 0 : region.getTax();
	}

	@Override
	public Bank getTaxBank()
	{
		Region region = getRegion();

		if(region == null)
		{
			log.warning("not found region for NPC " + getTemplateId() + " - " + getTemplateType());
			return null;
		}

		Guild owner = region.getOwner();
		return owner == null ? null : owner.getBank();
	}

	@Override
	public void setRegion(Region region)
	{
		this.region = region;
	}

	@Override
	public Region getRegion()
	{
		return region;
	}

	@Override
	public void setGuildOwner(Guild guild)
	{
	}

	@Override
	public Guild getGuildOwner()
	{
		return null;
	}
}
