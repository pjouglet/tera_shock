package tera.gameserver.scripts.commands;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import rlib.geoengine.GeoQuard;
import rlib.logging.GameLoggers;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.World;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.skillengine.Calculator;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.StatFunc;
import tera.gameserver.model.skillengine.funcs.stat.MathFunc;
import tera.gameserver.model.skillengine.lambdas.FloatMul;
import tera.gameserver.model.skillengine.lambdas.FloatSet;
import tera.gameserver.network.serverpackets.*;
import tera.gameserver.tables.NpcDialogTable;
import tera.gameserver.tables.SkillTable;
import tera.remotecontrol.handlers.LoadChatHandler;

/**
 * Обработчик команд для разработчиков.
 * 
 * @author Ronn
 */
public class DeveloperCommand extends AbstractCommand {

	private static final StatFunc SPEED = new MathFunc(StatType.RUN_SPEED, 0x45, null, new FloatSet(500));
	private static final StatFunc ATTACK = new MathFunc(StatType.ATTACK, 0x30, null, new FloatMul(50));

	public DeveloperCommand(int access, String[] commands) {
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values) {
		switch(command) {
			case "event_reg_all_players": {

				EventManager eventManager = EventManager.getInstance();
				Array<Player> players = World.getPlayers();

				players.readLock();
				try {

					for(Player target : players.array()) {

						if(target == null) {
							break;
						}

						eventManager.registerPlayer(values, target);
					}

				} finally {
					players.readUnlock();
				}

				break;
			}
			case  "zone":{
				player.sendMessage("ZONE :" + player.getZoneId());
				break;
			}
			case "change_class": {

				DataBaseManager dbManager = DataBaseManager.getInstance();

				PlayerClass cs = PlayerClass.valueOf(values);

				if(cs == player.getPlayerClass()) {
					return;
				}

				dbManager.updatePlayerClass(player.getObjectId(), cs);
				player.sendMessage("Player class have changed to " + cs);
				break;
			}
			case "kick": {

				Player target = World.getPlayer(values);

				if(target == null) {
					return;
				}

				player.sendMessage("игрок \"" + target.getName() + "\" кикнут.");
				target.getClient().close();
				return;
			}
			case "start_gc": {
				System.gc();
				break;
			}
			case "start_event": {
				EventManager eventManager = EventManager.getInstance();
				eventManager.startEvent(values);
				break;
			}
			case "a": {
				World.sendAnnounce(values);
				break;
			}
			case "reload_dialogs": {
				NpcDialogTable dialogTable = NpcDialogTable.getInstance();
				dialogTable.reload();
				break;
			}
			case "gm_speed": {
				player.addStatFunc(SPEED);
				player.updateInfo();
				break;
			}
			case "check_geo": {

				GeoManager geoManager = GeoManager.getInstance();
				GeoQuard[] quards = geoManager.getQuards(player.getContinentId(), player.getX(), player.getY());

				player.sendMessage("geo : " + Arrays.toString(quards));
				break;
			}
			case "send_state": {
				int val = Integer.parseInt(values);
				player.sendPacket(CharState.getInstance(player.getObjectId(), player.getSubId(), val), true);
				player.sendMessage("send state " + val);
				break;
			}
			case "add_attack": {
				ATTACK.addFuncTo(player);
				player.updateInfo();
				break;
			}
			case "send_system": {
				player.sendPacket(SystemMessage.getInstance(values.replace('&', (char) 0x0B)), true);
				break;
			}
			case "send_event": {
				player.sendPacket(EventMessage.getInstance(values, "", ""), true);
				break;
			}
			case "sub_attack": {
				ATTACK.removeFuncTo(player);
				player.updateInfo();
				break;
			}
			case "save_all": {

				DataBaseManager dbManager = DataBaseManager.getInstance();

				for(Player member : World.getPlayers()) {

					dbManager.fullStore(member);

					QuestList questList = member.getQuestList();
					questList.save();

					player.sendMessage("Character saved : \"" + member.getName() + "\"");
				}

				GameLoggers.finish();
				break;
			}
			case "save_point": {
				String point = "<point x=\"" + (int) player.getX() + "\" y=\"" + (int) player.getY() + "\" z=\"" + (int) player.getZ() + "\" heading=\"" + player.getHeading() + "\" />";
				LoadChatHandler.add(point);
				System.out.println(point);
				break;
			}
			case "get_my_id": {

				ByteBuffer buffer = ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN);
				buffer.clear();
				buffer.putInt(player.getObjectId());
				buffer.flip();

				StringBuilder text = new StringBuilder();

				for(byte byt : buffer.array()) {
					text.append(Integer.toHexString(byt & 0xFF)).append(" ");
				}

				player.sendMessage("Server: your object id " + text.toString());
				break;
			}
			case "my_funcs": {

				Calculator[] calcs = player.getCalcs();
				StringBuilder text = new StringBuilder("Funcs: ");

				for(Calculator calc : calcs) {
					if(calc != null && calc.getFuncs() != null && calc.getFuncs().size() > 0) {
						text.append(calc.getFuncs()).append(", ");
					}
				}

				player.sendMessage(text.toString());
				break;
			}
			case "invul": {
				player.setInvul(!player.isInvul());
				break;
			}
			case "send_bytes": {
				try {

					String[] strBytes = values.split(" ");

					List<Short> list = new ArrayList<Short>();

					for(int i = 0; i < strBytes.length; i++) {
						list.add(Short.parseShort(strBytes[i], 16));
					}

					player.sendPacket(SeverDeveloperPacket.getInstance(list), true);
				} catch(Exception e) {
					e.printStackTrace();
				}

				break;
			}
			case "send_file": {

				for(int i = 0; i < 10; i++) {

					File file = new File("./data/packets/packet" + (i == 0 ? "" : String.valueOf(i)) + ".txt");

					try(Scanner in = new Scanner(file)) {

						List<Short> list = new ArrayList<Short>();

						if(in.hasNext()) {
							for(String str = in.next(); in.hasNext(); str = in.next()) {
								list.add(Short.parseShort(str, 16));
							}
						}

						player.sendPacket(SeverDeveloperPacket.getInstance(list), true);
					} catch(IOException e) {
						break;
					}
				}

				for(int i = 0; i < 10; i++) {

					File file = new File("./data/packet" + (i == 0 ? "" : String.valueOf(i)) + ".txt");

					try(Scanner in = new Scanner(file)) {

						List<Short> list = new ArrayList<Short>();

						if(in.hasNext()) {
							for(String str = in.next(); in.hasNext(); str = in.next()) {
								list.add(Short.parseShort(str, 16));
							}
						}

						player.sendPacket(SeverDeveloperPacket.getInstance(list), true);
					} catch(IOException e) {
						break;
					}
				}

				break;
			}
			case "reload_skills": {
				SkillTable skillTable = SkillTable.getInstance();
				skillTable.reload();
				break;
			}
			case "set_level": {
				try {

					String[] vals = values.split(" ");

					if(vals.length < 1) {
						return;
					}

					byte level = Byte.parseByte(vals[0]);

					Player target = player;

					if(vals.length > 1) {
						target = World.getAroundByName(Player.class, player, vals[1]);
					}

					if(target == null) {
						return;
					}

					if(level > target.getLevel()) {

						for(int i = target.getLevel(); i < level; i++) {
							target.increaseLevel();
						}

					} else {
						target.setLevel(level);
						target.updateInfo();
					}

					DataBaseManager dbManager = DataBaseManager.getInstance();
					dbManager.updatePlayerLevel(target);

				} catch(NumberFormatException e) {
					Loggers.warning(getClass(), "error parsing " + values);
				}

				break;
			}
			case "set_access_level": {

				try {
					int val = Integer.parseInt(values);
					player.setAccessLevel(val);
				} catch(NumberFormatException e) {
					Loggers.warning(getClass(), "error parsing " + values);
				}

				break;
			}
			case "get_access_level": {
				player.sendMessage(String.valueOf(player.getAccessLevel()));
				break;
			}
			case "set_heart": {
				try {
					player.setStamina(Integer.parseInt(values));
					player.updateInfo();
				} catch(NumberFormatException e) {
					Loggers.warning(getClass(), "error parsing " + values);
				}
			}
		}
	}
}
