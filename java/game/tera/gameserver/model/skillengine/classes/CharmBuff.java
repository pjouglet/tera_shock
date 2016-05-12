package tera.gameserver.model.skillengine.classes;

import rlib.util.Rnd;
import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.worldobject.BonfireObject;
import tera.gameserver.network.serverpackets.CharmSmoke;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class CharmBuff extends Buff
{
	/** целевой костер */
	private BonfireObject bonfire;

	/**
	 * @param template темплейт скила.
	 */
	public CharmBuff(SkillTemplate template)
	{
		super(template);
	}

	@Override
	protected void addEffects(Character effector, Character effected)
	{
		// получаем игрока
		Player player = effected.getPlayer();

		if(player == null)
			return;

		// список возможных эффектов от скила
		EffectTemplate[] effectTemplates = template.getEffectTemplates();

		// если таких нет
		if(effectTemplates == null || effectTemplates.length == 0)
			return;

		EffectTemplate target = effectTemplates[Rnd.nextInt(0, effectTemplates.length - 1)];

		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// если эффект только на кастующего или шанс не сработал, пропускаем
		if(target.isOnCaster() || formulas.calcEffect(effector, effected, target, this) < 0)
			return;

		// активируем эффект
		runEffect(target.newInstance(effector, effected, template), effected);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// ищем костры вокруг
		Array<TObject> objects = World.getAround(BonfireObject.class, local.getNextObjectList(), attacker, 30 + attacker.getGeomRadius());

		// если костров рядом нет, выходим
		if(objects.isEmpty())
		{
			attacker.sendMessage(MessageType.CHARMS_CAN_ONLY_BE_USED_NEAR_A_CAMPFIRE);
			return false;
		}

		// запоминаем найденный костер
		setBonfire((BonfireObject) objects.first());

		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	private BonfireObject getBonfire()
	{
		return bonfire;
	}

	private void setBonfire(BonfireObject bonfire)
	{
		this.bonfire = bonfire;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем костер
		BonfireObject bonfire = getBonfire();

		// если он есть
		if(bonfire != null)
			// разворачиваем кастера к костру
			attacker.setHeading(attacker.calcHeading(bonfire.getX(), bonfire.getY()));

		super.startSkill(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем костер
		BonfireObject bonfire = getBonfire();

		// если его нет, выходим
		if(bonfire == null)
			return;

		// зануляем костер
		setBonfire(null);

		// отправляем пакет дыма
		character.broadcastPacket(CharmSmoke.getInstance(bonfire));

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// контейнер для целей
		Array<Character> targets = local.getNextCharList();

		// добавляем цели
		addTargets(targets, character, bonfire.getX(), bonfire.getY(), bonfire.getZ());

		Character[] array = targets.array();

		// перечисляем цели
		for(int i = 0, length = targets.size(); i < length; i++)
    	{
			Character target = array[i];

			// если цель мертва или в инву, пропускаем
    		if(target.isDead() || target.isInvul())
    			continue;

    		// применяем скил
    		applySkill(character, target);
    	}
	}
}
