package tera.gameserver.model.npc;

import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWarNpc;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.Guild;
import tera.gameserver.model.npc.playable.PlayerKiller;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель защитного НПС в битве за регион.
 *
 * @author Ronn
 */
public class RegionWarDefense extends PlayerKiller implements RegionWarNpc
{
	/** владеющий регион */
	private Region region;

	public RegionWarDefense(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkTarget(Character target)
	{
		// если это контрол
		if(target.getClass() == RegionWarControl.class)
		{
			// получаем контрол
			RegionWarControl control = (RegionWarControl) target;

			// является враг, если он был захвачен
			return control.getGuildOwner() != null;
		}

		// если это НПС, значит свой
		if(target.isNpc() && !target.isSummon())
			return false;

		Player player = null;

		if(target.isSummon())
		{
			Character owner = target.getOwner();

			if(owner != null && owner.isPlayer())
				player = owner.getPlayer();
		}
		else if(target.isPlayer())
			player = target.getPlayer();

		// если это игрок или питомец игрока, значит враг
		return player != null;
	}

	@Override
	public void causingDamage(Skill skill, AttackInfo info, Character attacker)
	{
		// определяем атакующего игрока
		Player player = null;

		if(attacker.isSummon())
		{
			Character owner = attacker.getOwner();

			if(owner != null && owner.isPlayer())
				player = owner.getPlayer();
		}
		else if(attacker.isPlayer())
			player = attacker.getPlayer();

		// если игрока нет, урон не засчитываем
		if(player == null)
			return;

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если гильдии нет либо она не из этой битвы, не засчитываем урон
		if(guild == null || !getRegion().isRegister(guild))
			return;

		// обрабатываем урон
		super.causingDamage(skill, info, attacker);
	}

	@Override
	public void addAggro(Character aggressor, long aggro, boolean damage)
	{
		// если это не урон от игрока
		if(!damage && aggressor.isPlayer())
		{
			// получаем игрока
			Player player = aggressor.getPlayer();

			// в зависимости от класса игрока
			switch(player.getPlayerClass())
			{
				case WARRIOR:
				case LANCER: aggro = 1; break;
				case MYSTIC:
				case PRIEST: aggro *= 3; break;
				default : break;
			}
		}

		// обрабатываем добавление аггро
		super.addAggro(aggressor, aggro, damage);
	}

	@Override
	public void setGuildOwner(Guild guild){}

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
		return null;
	}
}
