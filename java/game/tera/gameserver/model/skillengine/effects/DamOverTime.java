package tera.gameserver.model.skillengine.effects;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.Duel;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, который наносит промежуточно урон.
 *
 * @author Ronn
 */
public class DamOverTime extends AbstractEffect
{
	/**
	 * @param template темплейт эффекта.
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скил, которым наложили.
	 */
	public DamOverTime(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	protected int getDamage(Character effector, Character effected)
	{
		return getTemplate().getPower();
	}

	@Override
	public boolean onActionTime()
	{
		// получаем того, на ком висит эффект
		Character effected = getEffected();

		// получем того, кто повесил эффект
		Character effector = getEffector();

		// если его нет, выходим
		if(effected == null || effector == null)
		{
			LOGGER.warning(this, new Exception("not found effector of effected"));
			return false;
		}

		// если цель уже мертва, выходим
		if(effected.isDead())
			return false;

		// получаем силу ДоТа
		int damage = getDamage(effector, effected);

		// получаем дуэль персонажа
		Duel duel = effected.getDuel();

		if(damage > effected.getCurrentHp() - 2)
			return false;

		// если хп недостаточно у персонажа, останавливаем дот и выходим
		if(damage > effected.getCurrentHp() && duel != null && effector.getDuel() == duel)
		{
			// завершаем дуэль
			duel.finish();

			// снимаем ДоТ
			return false;
		}

		// применяем дмг
		effected.setCurrentHp(effected.getCurrentHp() - damage);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем об атаке
		eventManager.notifyAttacked(effected, effector, null, damage, false);
		// уведомляем о нанесении урона
		eventManager.notifyHpChanged(effected);

		// отправляем пакет с уроном
		effected.broadcastPacket(Damage.getInstance(effector, effected, getSkillId(), damage, false, false, Damage.DAMAGE));

		/*// если цель мертва
		if(effected.isDead())
		{
			// убиваем ее
			effected.doDie(effector);

			// выходим с прекращением эффекта
			return false;
		}*/

		return true;
	}
}
