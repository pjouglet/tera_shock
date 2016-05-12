package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;

/**
 * Модель для реализации рассчета целей в области от точки применения.
 *
 * @author Ronn
 */
public class AuraFractionTargetHandler extends AuraTargetHandler
{
	@Override
	protected void addAllTargets(Array<Character> targets, Character caster, int radius)
	{
		World.getAround(Npc.class, targets, caster, radius).add(caster);
	}

	@Override
	protected boolean checkTarget(Character caster, Character target)
	{
		if(caster == target)
			return true;

		// если кто-то из них не нпс, цель не возможна
		if(!caster.isNpc() || !target.isNpc())
			return false;

		// получаем нпс кастующего
		Npc casterNpc = caster.getNpc();
		// получаем нпс цель
		Npc targetNpc = target.getNpc();

		// являются ли они одной фракции
		return casterNpc.getFraction().equals(targetNpc.getFraction());
	}
}
