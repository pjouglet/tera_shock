package tera.gameserver.model.npc;

import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWarNpc;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.Guild;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.TargetHp;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель регионального контрола.
 *
 * @author Ronn
 */
public class RegionWarControl extends Monster implements RegionWarNpc
{
	/** владеющая гильдия */
	private Guild guild;

	/** регион, к которому принадлежит */
	private Region region;

	public RegionWarControl(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void setGuildOwner(Guild guild)
	{
		this.guild = guild;
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
	public Guild getGuildOwner()
	{
		return guild;
	}

	@Override
	public boolean isOwerturnImmunity()
	{
		return true;
	}

	@Override
	public boolean isStunImmunity()
	{
		return true;
	}

	@Override
	public boolean isSleepImmunity()
	{
		return true;
	}

	@Override
	public boolean isLeashImmunity()
	{
		return true;
	}

	@Override
	public void causingDamage(Skill skill, AttackInfo info, Character attacker)
	{
		// если атакуемый персонаж игрок
		if(attacker.isPlayer())
		{
			// получаем гильдию атакующего
			Guild guild = attacker.getGuild();

			// если это гильдия не зарегестрирована в этой битве, игнорируем атакующего
			if(!region.isRegister(guild) || getGuildOwner()  == guild)
				return;
		}

		super.causingDamage(skill, info, attacker);
	}

	@Override
	public void updateHp()
	{
		super.updateHp();

		// получаем владельца
		Guild owner = getGuildOwner();

		// если он есть
		if(owner != null)
			// отправляем всем состояние хп
			owner.sendPacket(null, TargetHp.getInstance(this, TargetHp.BLUE));
	}
}
