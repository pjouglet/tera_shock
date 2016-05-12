package tera.gameserver.scripts.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import rlib.util.Files;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.OnlineManager;
import tera.gameserver.manager.PlayerManager;
import tera.gameserver.model.Account;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.SkillListInfo;

/**
 * Список команд, доступных для всех игроков.
 * 
 * @author Ronn
 */
public class UserCommand extends AbstractCommand {

	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private static final String CHANGE_APPEARANCE_VAR = "CHANGE_APPEARANCE_VAR";
	private static final String CHANGE_RACE_VAR = "CHANGE_RACE_VAR";

	private static final int CHANGE_APPEARANCE_LIMIT = 3;
	private static final int CHANGE_RACE_LIMIT = 3;

	private static final Date date = new Date();

	private static String help;

	public UserCommand(int access, String[] commands) {
		super(access, commands);

		help = Files.read(Config.SERVER_DIR + "/data/help.txt");
	}

	@Override
	public void execution(String command, Player player, String values) {
		switch(command) {
			case "event_reg": {

				if(values == null) {
					return;
				}

				EventManager eventManager = EventManager.getInstance();
				eventManager.registerPlayer(values, player);
				break;
			}
			case "restore_characters": {
				PlayerManager playerManager = PlayerManager.getInstance();
				playerManager.restoreCharacters(player);

				break;
			}
			case "help":
				player.sendMessage(help);
				break;
			case "time": {
				synchronized(date) {
					date.setTime(System.currentTimeMillis());
					player.sendMessage(timeFormat.format(date));
				}

				break;
			}
			case "end_pay": {
				Account account = player.getAccount();

				long time = account.getEndPay();

				if(System.currentTimeMillis() > time)
					player.sendMessage("У вас не проплаченный аккаунт.");
				else {
					synchronized(date) {
						date.setTime(time);
						player.sendMessage("Дата завершения проплаты: " + timeFormat.format(date));
					}
				}

				break;
			}
			case "restore_skills": {
				Table<IntKey, Skill> current = player.getSkills();

				DataBaseManager dbManager = DataBaseManager.getInstance();

				for(Skill skill : current) {
					if(skill.getClassId() == -15)
						continue;

					player.removeSkill(skill, false);

					dbManager.deleteSkill(player, skill);

					skill.fold();
				}

				current.clear();

				player.getTemplate().giveSkills(player);
				player.sendPacket(SkillListInfo.getInstance(player), true);
				break;
			}
			case "kill_me": {
				if(player.isBattleStanced()) {
					player.sendMessage("Нельзя использовать в бою.");
					return;
				}

				synchronized(player) {
					player.setCurrentHp(0);
					player.doDie(player);
				}

				break;
			}
			case "version":
				player.sendMessage("Текущая версия сервера: " + Config.SERVER_VERSION);
				break;
			case "online": {
				OnlineManager onlineManager = OnlineManager.getInstance();

				player.sendMessage("Текущий онлаин: " + onlineManager.getCurrentOnline());

				break;
			}
			case "player_info": {
				if(player.getName().equals(values))
					return;

				Player target = World.getAroundByName(Player.class, player, values);

				if(target == null)
					target = World.getPlayer(values);

				if(target == null) {
					player.sendMessage(MessageType.THAT_CHARACTER_ISNT_ONLINE);
					return;
				}

				StringBuilder builder = new StringBuilder("\n--------\nPlayer: \"").append(target.getName()).append("\":\n");

				builder.append("attack:").append(target.getAttack(null, null)).append(";\n");
				builder.append("defense:").append(target.getDefense(null, null)).append(";\n");
				builder.append("impact:").append(target.getImpact(null, null)).append(";\n");
				builder.append("balance:").append(target.getBalance(null, null)).append(";\n");
				builder.append("max hp:").append(target.getMaxHp()).append(".\n");
				builder.append("--------");

				player.sendMessage(builder.toString());

				break;
			}
		}
	}
}
