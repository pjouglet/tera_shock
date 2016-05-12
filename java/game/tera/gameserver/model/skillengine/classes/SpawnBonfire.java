package tera.gameserver.model.skillengine.classes;

import rlib.geom.Coords;
import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.worldobject.BonfireObject;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Скилл для спавна костров.
 *
 * @author Ronn
 */
public class SpawnBonfire extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public SpawnBonfire(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// ищем костры вокруг
		Array<TObject> bonfires = World.getAround(BonfireObject.class, local.getNextObjectList(), attacker, 200);

		if(!bonfires.isEmpty())
		{
			attacker.sendMessage(MessageType.THERE_ANOTHER_CAMPFIRE_NEAR_HERE);
			return false;
		}

		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		float x = Coords.calcX(character.getX(), 5, character.getHeading());
		float y = Coords.calcY(character.getY(), 5, character.getHeading());

		BonfireObject.startBonfire(template.getRegenPower(), template.getLifeTime(), character.getContinentId(), x, y, character.getZ());
	}
}
