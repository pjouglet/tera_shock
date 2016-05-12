package tera.gameserver.model.skillengine.classes;

import rlib.util.VarTable;
import rlib.util.array.Array;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.spawn.SummonSpawn;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Скил для спавна тени варриора.
 * 
 * @author Ronn
 */
public class SpawnSmokeSummon extends AbstractSkill {

	/** спавн суммона */
	private volatile SummonSpawn spawn;

	public SpawnSmokeSummon(SkillTemplate template) {
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ) {
		super.useSkill(character, targetX, targetY, targetZ);

		if(!character.isPlayer()) {
			return;
		}

		int consume = character.getMaxHp() * 20 / 100;

		if(character.getCurrentHp() <= consume + 2) {
			character.sendMessage("У вас не достаточно HP.");
			return;
		}

		SummonSpawn spawn = getspawn();

		if(spawn == null) {
			character.sendMessage("Этот суммон не реализован.");
			return;
		}

		Summon summon = character.getSummon();

		if(summon != null) {
			summon.remove();
		}

		spawn.setOwner(character);
		spawn.getLocation().setXYZHC(character.getX(), character.getY(), character.getZ(), character.getHeading(), character.getContinentId());
		spawn.start();

		summon = character.getSummon();

		character.setCurrentHp(character.getCurrentHp() - consume);

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyHpChanged(character);

		Array<Npc> hateList = character.getLocalHateList();

		for(Npc npc : hateList.array()) {
			if(npc == null) {
				break;
			}

			long hate = Math.max(npc.getAggro(character), 1);

			npc.removeAggro(character);
			npc.addAggro(summon, hate, false);
		}
	}

	protected SummonSpawn getspawn() {
		if(spawn == null) {
			synchronized(this) {
				if(spawn == null) {

					NpcTable npcTable = NpcTable.getInstance();

					NpcTemplate temp = npcTable.getTemplate(template.getSummonId(), template.getSummonType());

					if(temp == null) {
						return null;
					}

					ConfigAITable configTable = ConfigAITable.getInstance();

					VarTable vars = template.getVars();

					ConfigAI configAI = configTable.getConfig(vars.getString("configAI", null));

					if(configAI == null) {
						return null;
					}

					spawn = new SummonSpawn(temp, configAI, vars.getEnum("aiClass", NpcAIClass.class), template.getLifeTime());
				}
			}
		}

		return spawn;
	}
}
