package tera.gameserver.model.skillengine.classes;

import rlib.geom.Coords;
import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.territory.BonfireTerritory;
import tera.gameserver.tables.BonfireTable;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class TeleportNearBonfire extends AbstractSkill
{
	private Location loc;

	/**
	 * @param template темплейт скила.
	 */
	public TeleportNearBonfire(SkillTemplate template)
	{
		super(template);

		this.loc = new Location();
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		Array<Character> targets = local.getNextCharList();

		addTargets(targets, character, targetX, targetY, targetZ);

		if(targets.isEmpty())
			return;

		BonfireTerritory near = BonfireTable.getNearBonfire(character);

		if(near == null)
			return;

		for(Character player : targets)
			if(!player.isDead())
				player.teleToLocation(Coords.randomCoords(loc, near.getCenterX(), near.getCenterY(), near.getCenterZ(), 60, 150));
	}
}
