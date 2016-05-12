package tera.gameserver.model.skillengine.classes;

import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.spawn.SummonSpawn;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Скил для спавна сумона.
 * 
 * @author Ronn
 */
public class SpawnSummon extends AbstractSkill
{
	/** спавн суммона */
	private volatile SummonSpawn spawn;

	public SpawnSummon(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		super.useSkill(character, targetX, targetY, targetZ);

		SummonSpawn spawn = getspawn();

		if(spawn == null)
		{
			character.sendMessage("Этот суммон не реализован.");
			return;
		}

		Summon summon = character.getSummon();

		if(summon != null)
			summon.remove();

		spawn.setOwner(character);
		spawn.getLocation().setXYZHC(character.getX(), character.getY(), character.getZ(), character.getHeading(), character.getContinentId());
		spawn.start();
	}

	protected SummonSpawn getspawn()
	{
		if(spawn == null)
			synchronized(this)
			{
				if(spawn == null)
				{
					NpcTable npcTable = NpcTable.getInstance();

					NpcTemplate temp = npcTable.getTemplate(template.getSummonId(), template.getSummonType());

					if(temp == null)
						return null;

					ConfigAITable configTable = ConfigAITable.getInstance();

					VarTable vars = template.getVars();

					ConfigAI configAI = configTable.getConfig(vars.getString("configAI", null));

					if(configAI == null)
						return null;

					spawn = new SummonSpawn(temp, configAI, vars.getEnum("aiClass", NpcAIClass.class), template.getLifeTime());
				}
			}

		return spawn;
	}
}
