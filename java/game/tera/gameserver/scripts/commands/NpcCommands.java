package tera.gameserver.scripts.commands;

import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.World;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.links.NpcLink;
import tera.gameserver.model.npc.spawn.NpcSpawn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.NpcDialogWindow;
import tera.gameserver.network.serverpackets.NpcState;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.tables.SpawnTable;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Список команд, для работы с нпс.
 * 
 * @author Ronn
 */
public class NpcCommands extends AbstractCommand
{
	/**
	 * @param access
	 * @param commands
	 */
	public NpcCommands(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		SpawnTable spawnTable = SpawnTable.getInstance();

		switch(command)
		{
			case "stop_spawns":
				spawnTable.stopSpawns();
				break;
			case "start_spawns":
				spawnTable.startSpawns();
				break;
			case "send_dialog":
			{
				Array<Link> array = Arrays.toArray(Link.class);

				int id = Integer.parseInt(values);

				for(int i = id, length = id + 10; i < length; i++)
					array.add(new NpcLink("@npc:" + i, LinkType.DIALOG, IconType.DIALOG, null));

				player.sendPacket(NpcDialogWindow.getInstance(null, player, array), true);

				break;
			}
			case "npc_cast":
			{
				int skillId = Integer.parseInt(values);

				Array<Npc> npcs = World.getAround(Npc.class, player, 300F);

				for(final Npc npc : npcs)
				{
					Skill skill = npc.getSkill(skillId);

					if(skill == null)
						continue;

					if(npc.isCastingNow())
						npc.abortCast(true);

					npc.setTarget(player);

					npc.getAI().startCast(skill, npc.calcHeading(player.getX(), player.getY()), player.getX(), player.getY(), player.getZ());
				}

				break;
			}
			case "around_npc_spawn":
			{
				Array<Npc> npcs = World.getAround(Npc.class, player, 100F);

				StringBuilder text = new StringBuilder("Spawns:");

				for(Npc npc : npcs)
				{
					if(npc == null || !(npc.getSpawn() instanceof NpcSpawn))
						continue;

					NpcSpawn spawn = (NpcSpawn) npc.getSpawn();

					text.append(" [id = ").append(spawn.getTemplateId()).append(", ").append(spawn.getLocation()).append("], ");
				}

				if(text.length() > 7)
				{
					text.replace(text.length() - 2, text.length(), ".");
					player.sendMessage(text.toString());
				}
				else
					player.sendMessage("no npcs.");

				break;
			}
			case "reload_spawns":
			{
				try
				{
					spawnTable.reload();
					player.sendMessage("NPC spanws have been reloaded.");
				}
				catch(Exception e)
				{
					Loggers.warning(getClass(), e);
				}

				break;
			}
			case "around_npc_cast":
			{
				final int id = Integer.decode(values);

				Array<Npc> npcs = World.getAround(Npc.class, player, 150F);

				for(final Npc npc : npcs)
				{
					npc.broadcastPacket(SkillStart.getInstance(npc, id, 1, 0));

					player.sendMessage("cast " + id + " to " + npc);

					Runnable run = new Runnable()
					{
						@Override
						public void run()
						{
							npc.broadcastPacket(SkillEnd.getInstance(npc, 1, id));
						}
					};

					ExecutorManager executor = ExecutorManager.getInstance();
					executor.scheduleGeneral(run, 1000);
				}

				break;
			}
			case "around_npc_long_cast":
			{
				final int id = Integer.parseInt(values);

				Array<Npc> npcs = World.getAround(Npc.class, player, 100F);

				for(final Npc npc : npcs)
				{
					npc.broadcastPacket(SkillStart.getInstance(npc, id, 0, 0));

					Runnable run = new Runnable()
					{
						@Override
						public void run()
						{
							npc.broadcastPacket(SkillEnd.getInstance(npc, 0, id));
						}
					};

					ExecutorManager executor = ExecutorManager.getInstance();
					executor.scheduleGeneral(run, 4000);
				}

				break;
			}
			case "reload_npcs":
			{
				NpcTable npcTable = NpcTable.getInstance();
				npcTable.reload();

				player.sendMessage("NPCs have been reloaded.");

				break;
			}
			case "spawn":
			{
				String vals[] = values.split(" ");

				int id = Integer.parseInt(vals[0]);
				int type = Integer.parseInt(vals[1]);

				NpcTable npcTable = NpcTable.getInstance();

				NpcTemplate template = npcTable.getTemplate(id, type);

				if(template == null)
				{
					player.sendMessage("Target NPC doesn't exist.");
					return;
				}

				String aiConfig = vals.length > 2 ? vals[2] : "DefaultMonster";

				ConfigAITable configTable = ConfigAITable.getInstance();

				NpcSpawn spawn = new NpcSpawn(null, null, template, player.getLoc(), 45, 0, 120, 0, configTable.getConfig(aiConfig), NpcAIClass.DEFAULT);

				int respawnTime = Integer.MAX_VALUE / 2000;

				spawn.setRespawnTime(respawnTime);
				spawn.start();

				break;
			}
			case "go_to_npc":
			{
				String[] vals = values.split(" ");

				Location loc = spawnTable.getNpcSpawnLoc(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));

				if(loc != null)
					player.teleToLocation(loc);

				break;
			}
			case "around_npc":
			{
				try
				{
					Array<Npc> npcs = World.getAround(Npc.class, player, 150F);

					StringBuilder text = new StringBuilder("Npcs:");

					for(Npc npc : npcs)
					{
						if(npc == null)
							continue;

						text.append(" id = ").append(npc.getTemplateId()).append(", type = ").append(npc.getTemplateType()).append(", objectId = ").append(npc.getObjectId()).append(", ");
						// text.append(" id = ").append(npc.getNpcId()).append(npc.getSpawnLoc());

						npc.broadcastPacket(NpcState.getInstance(player, npc, 1));
					}

					if(text.length() > 5)
					{
						text.replace(text.length() - 2, text.length(), ".");
						player.sendMessage(text.toString());
					}
					else
						player.sendMessage("no npcs.");
				}
				catch(Exception e)
				{
					Loggers.warning(getClass(), "error " + command + " vals " + values + " " + e.getMessage());
				}

				break;
			}
		}
	}
}
