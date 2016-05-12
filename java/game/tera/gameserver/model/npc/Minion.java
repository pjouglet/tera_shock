package tera.gameserver.model.npc;

import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import tera.gameserver.model.Character;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Модель миниона.
 *
 * @author Ronn
 */
public class Minion extends Monster
{
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(600001, 800000);

	/** лидер миниона */
	private MinionLeader leader;

	public Minion(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void doDie(Character attacker)
	{
		MinionLeader leader = getMinionLeader();

		if(leader != null)
			leader.onDie(this);

		super.doDie(attacker);
	}

	@Override
	public boolean isMinion()
	{
		return true;
	}

	@Override
	public int nextCastId()
	{
		return ID_FACTORY.getNextId();
	}

	/**
	 * @param leader лидер миниона.
	 */
	public final void setLeader(MinionLeader leader)
	{
		this.leader = leader;
	}

	/**
	 * @param spawnLoc точка спавна.
	 * @param leader лидер миниона.
	 */
	public void spawnMe(Location spawnLoc, MinionLeader leader)
	{
		// запоминаем лидера
		setLeader(leader);

		// спавнимся
		spawnMe(spawnLoc);

		// обновляем реальную спавн точку
		getSpawnLoc().set(leader.getSpawnLoc());
	}

	@Override
	public int getKarmaMod()
	{
		return 0;
	}

	@Override
	public MinionLeader getMinionLeader()
	{
		return leader;
	}
}
