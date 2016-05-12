package tera.gameserver.model.skillengine.classes;

import rlib.geom.Coords;
import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель спавнящих итемы скилов.
 *
 * @author Ronn
 */
public class SpawnItem extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public SpawnItem(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// создаем нужные нам итемы
		ItemInstance item = ItemTable.createItem(getItemId(), (long) getItemCount());

		// указываем кто их спавнит
		item.setDropper(character);
		// указываем кто владелец
		item.setTempOwner(character);
		// указываем чья пати владелец
		item.setTempOwnerParty(character.getParty());

		// рассчитываем точку спавна
		float newX = Coords.calcX(character.getX(), 30, character.getHeading());
		float newY = Coords.calcY(character.getY(), 30, character.getHeading());

		// указываем континент
		item.setContinentId(character.getContinentId());

		// спавним
		item.spawnMe(newX, newY, character.getZ(), 0);
	}
}
