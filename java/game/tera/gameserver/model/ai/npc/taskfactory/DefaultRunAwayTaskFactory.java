package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Coords;
import rlib.util.VarTable;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Дефолтная модель фабрики заданий при убегании НПС.
 *
 * @author Ronn
 */
public class DefaultRunAwayTaskFactory extends AbstractTaskFactory
{
	/** отступ от атакущего */
	protected final int offset;

	public DefaultRunAwayTaskFactory(Node node)
	{
		super(node);

		VarTable vars = VarTable.newInstance(node, "set", "name", "val");

		this.offset = vars.getInteger("offset", ConfigAI.DEFAULT_RUN_AWAY_OFFSET);
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем текущую цель
		Character target = ai.getTarget();

		// если ее нет или она мертва, выходим
		if(target == null || target.isDead() || !actor.isInRange(target, getOffset()))
			return;

		// расчитываем направление от нее к цели
		int heading = target.calcHeading(actor.getX(), actor.getY());

		// рассчитываем целевую точку
		float targetX = Coords.calcX(target.getX(), getOffset(), heading);
		float targetY = Coords.calcY(target.getY(), getOffset(), heading);

		// получаем менеджер геодаты
		GeoManager geoManager = GeoManager.getInstance();

		float targetZ = geoManager.getHeight(actor.getContinentId(), targetX, targetY, actor.getZ());

		if(actor.getRunSpeed() > 10)
			// добавляем задание бежать в указанную точку
			ai.addMoveTask(targetX, targetY, targetZ, true);
		else
			actor.teleToLocation(actor.getContinentId(), targetX, targetY, targetZ);
	}

	/**
	 * @return отступ в направлении.
	 */
	protected final int getOffset()
	{
		return offset;
	}
}
