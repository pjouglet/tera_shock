package tera.gameserver.model.npc.summons;

import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import tera.gameserver.IdFactory;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.network.serverpackets.TargetHp;
import tera.gameserver.templates.NpcTemplate;

/**
 * Базовая модель суммона.
 * 
 * @author Ronn
 */
public class Summon extends Npc
{
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(1300001, 1600000);

	/** владелец сумона */
	protected Character owner;

	public Summon(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void addAggro(Character aggressor, long aggro, boolean damage)
	{
	}

	@Override
	public void subAggro(Character aggressor, long aggro)
	{
	}

	@Override
	public Character getMostDamager()
	{
		return null;
	}

	@Override
	public void clearAggroList()
	{
	}

	@Override
	public long getAggro(Character aggressor)
	{
		return 0;
	}

	@Override
	public boolean checkTarget(Character target)
	{
		Character owner = getOwner();

		if(owner != null)
			return owner.checkTarget(target);

		return false;
	}

	@Override
	protected void addCounter(Character attacker)
	{
	}

	@Override
	public void doDie(Character attacker)
	{
		Character owner = getOwner();

		if(owner != null && owner.isPlayer())
		{
			if(owner == attacker)
				owner.sendMessage(MessageType.YOUR_PET_HAS_BEEN_DESTRUYED);
			else
				owner.sendPacket(SystemMessage.getInstance(MessageType.ATTACKER_DESTROYED_YOUR_PET).addAttacker(attacker.getName()), true);
		}

		if(owner != null)
			owner.setSummon(null);

		super.doDie(attacker);

		getAI().stopAITask();
	}

	@Override
	protected void calculateRewards(Character killer)
	{
	}

	@Override
	public Character getMostHated()
	{
		return null;
	}

	@Override
	public void removeAggro(Character agressor)
	{
	}

	@Override
	public int getOwerturnTime()
	{
		return 3500;
	}

	@Override
	public final boolean isSummon()
	{
		return true;
	}

	@Override
	public void reinit()
	{
		IdFactory idFactory = IdFactory.getInstance();

		setObjectId(idFactory.getNextNpcId());

		getAI().startAITask();
	}

	/**
	 * Удаление сумона.
	 */
	public void remove()
	{
		if(!isDead())
		{
			setCurrentHp(0);
			doDie(owner);
		}
	}

	@Override
	public void setOwner(Character owner)
	{
		this.owner = owner;
	}

	@Override
	public Character getOwner()
	{
		return owner;
	}

	@Override
	public void updateHp()
	{
		Character owner = getOwner();

		if(owner != null && owner.isPlayer())
			owner.sendPacket(TargetHp.getInstance(this, TargetHp.BLUE), true);
	}

	@Override
	public int nextCastId()
	{
		return ID_FACTORY.getNextId();
	}

	@Override
	public void effectHealHp(int heal, Character healer)
	{
		super.effectHealHp(heal, healer);

		updateHp();
	}

	@Override
	public void doRegen()
	{
		int currentHp = getCurrentHp();

		super.doRegen();

		if(currentHp != getCurrentHp())
			updateHp();
	}
}
