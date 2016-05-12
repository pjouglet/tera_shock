package tera.gameserver.model.skillengine.classes;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила для восстановления стамины.
 *
 * @author Ronn
 */
public class RestoreStamina extends ItemBuff
{
	public RestoreStamina(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		super.useSkill(character, targetX, targetY, targetZ);

		// если цель не игрок, дальше не идем
		if(!character.isPlayer())
			return;

		// получаем игрока
		Player player = character.getPlayer();

		// добавляем ему стамины
		player.setStamina(player.getStamina() + getPower());

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о изменении стамины
		eventManager.notifyStaminaChanged(player);
	}
}
