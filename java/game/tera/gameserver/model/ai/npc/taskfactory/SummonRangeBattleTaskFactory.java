package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.VarTable;

import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Модель фабрики заданий для суммона в бою НПС.
 *
 * @author Ronn
 */
public class SummonRangeBattleTaskFactory extends SummonBattleTaskFactory
{
	/** рейт отбегание от цели */
	protected final int runAwayRate;

	public SummonRangeBattleTaskFactory(Node node)
	{
		super(node);

		try
		{
			// парсим атрибуты
			VarTable vars = VarTable.newInstance(node);

			this.runAwayRate = vars.getInteger("runAwayRate", 20);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем текущую цель
		Character target = ai.getTarget();

		// если ее нет, выходим
		if(target == null)
			return;

		int shortRange = getShortRange();

		if(actor.isInRange(target, shortRange) && Rnd.chance(getRunAwayRate()))
		{
			int newDist = shortRange * 2;

			float radians = Angles.headingToRadians(target.calcHeading(actor.getX(), actor.getY()) + Rnd.nextInt(-6000, 6000));

			float newX = Coords.calcX(target.getX(), newDist, radians);
			float newY = Coords.calcY(target.getY(), newDist, radians);

			GeoManager geoManager = GeoManager.getInstance();

			float newZ = geoManager.getHeight(target.getContinentId(), newX, newY, target.getZ());

			ai.addMoveTask(newX, newY, newZ, true);
			return;
		}

		super.addNewTask(ai, actor, local, config, currentTime);
	}

	/**
	 * @return шанс получения задачи отбегания.
	 */
	public int getRunAwayRate()
	{
		return runAwayRate;
	}
}
