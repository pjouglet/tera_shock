package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.network.ServerPacketType;
import tera.gameserver.templates.PlayerTemplate;

/**
 * Пакет с информацией о игроке.
 *
 * @author Ronn
 */
public class UserInfo extends ServerPacket
{
	private static final ServerPacket instance = new UserInfo();

	public static UserInfo getInstance(Player player)
	{
		UserInfo packet = (UserInfo) instance.newInstance();

		// получаем подготавливаемый буффер
		ByteBuffer buffer = packet.getPrepare();

		try
		{
			// получаем шаблон игрока
			PlayerTemplate template = player.getTemplate();

			int attack = player.getAttack(null, null);
			int baseAttack = player.getBaseAttack();

			int defense = player.getDefense(null, null);
			int baseDefense = player.getBaseDefense();

			int impact = player.getImpact(null, null);
			int baseImpact = player.getBaseImpact();

			int balance = player.getBalance(null, null);
			int baseBalance = player.getBaseBalance();

			float weakResist = player.calcStat(StatType.WEAK_RECEPTIVE, 0, 0x20, null, null);
			float stunResist = player.calcStat(StatType.STUN_RECEPTIVE, 0, 0x20, null, null);
			float dmgResist = player.calcStat(StatType.DAMAGE_RECEPTIVE, 0, 0x20, null, null);

			packet.writeInt(buffer, player.getCurrentHp()); // ХП сколько есть
			packet.writeInt(buffer, player.getCurrentMp()); // МП сколько есть

			packet.writeInt(buffer, 0);

			packet.writeInt(buffer, player.getMaxHp());
			packet.writeInt(buffer, player.getMaxMp());
			packet.writeInt(buffer, template.getPowerFactor());
			packet.writeInt(buffer, template.getDefenseFactor());
			packet.writeShort(buffer, template.getImpactFactor());
			packet.writeShort(buffer, template.getBalanceFactor());
			packet.writeShort(buffer, template.getRunSpd()); // базовая скорость бега
			packet.writeShort(buffer, template.getAtkSpd());

			packet.writeFloat(buffer, template.getCritRate()); // шанс крита
			packet.writeFloat(buffer, template.getCritRcpt()); // защита от крита (пока не ясно кд или шанс режет)
			packet.writeFloat(buffer, 2); // крит дамаг
			packet.writeInt(buffer, baseAttack); // базовая атака мин
			packet.writeInt(buffer, baseAttack); // базовая атака макс
			packet.writeInt(buffer, baseDefense);
			packet.writeShort(buffer, baseImpact);
			packet.writeShort(buffer, baseBalance);
			packet.writeFloat(buffer, weakResist);// (Hex)Сопротивление к Ядам 38
			packet.writeFloat(buffer, dmgResist);// (Hex)Сопротивление к повреждениям 38
			packet.writeFloat(buffer, stunResist);// (Hex)Сопротивление к обиздвиживанию 38
			packet.writeInt(buffer, player.getPowerFactor() - template.getPowerFactor()); // бонус к повер фактору
			packet.writeInt(buffer, player.getDefenseFactor() - template.getDefenseFactor()); // бонус к дефенс фактору
			packet.writeShort(buffer, player.getImpactFactor() - template.getImpactFactor()); // бонус к импакт фактору
			packet.writeShort(buffer, player.getBalanceFactor() - template.getBalanceFactor()); // бонус к баланс фактору
			packet.writeShort(buffer, player.getRunSpeed() - template.getRunSpd()); // Бонус к скорости бега...
			packet.writeShort(buffer, player.getAtkSpd() - template.getAtkSpd()); // бонус к атак спиду

			packet.writeFloat(buffer, player.getCritRate(null, null) - template.getCritRate()); // крит рейт бонус
			packet.writeFloat(buffer, player.getCritRateRcpt(null, null) - template.getCritRcpt()); // крит ресист бонус
			packet.writeFloat(buffer, player.getCritDamage(null, null) - 2); // крит мощность бонус
			packet.writeInt(buffer, attack - baseAttack); // бонус к атаке мин
			packet.writeInt(buffer, attack - baseAttack); // бонус к атаке макс
			packet.writeInt(buffer, defense - baseDefense); // бонус к защите
			packet.writeShort(buffer, impact - baseImpact); // бонус к импакту
			packet.writeShort(buffer, balance - baseBalance); // бонус к балансу

			packet.writeFloat(buffer, player.calcStat(StatType.WEAK_RECEPTIVE, 0, null, null) - weakResist);
			packet.writeFloat(buffer, player.calcStat(StatType.DAMAGE_RECEPTIVE, 0, null, null) - dmgResist);
			packet.writeFloat(buffer, player.calcStat(StatType.STUN_RECEPTIVE, 0, null, null) - stunResist);

			packet.writeShort(buffer, player.getLevel());
			packet.writeShort(buffer, player.isBattleStanced() ? 1 : 0);
			packet.writeShort(buffer, 4);
			packet.writeByte(buffer, 1);
			packet.writeInt(buffer, player.getMaxHp() - player.getBaseMaxHp());
			packet.writeInt(buffer, player.getMaxMp() - player.getBaseMaxMp());
			packet.writeInt(buffer, player.getStamina());
			packet.writeInt(buffer, player.getMaxStamina());
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, player.getKarma());// карма

			packet.writeInt(buffer, 0);// item level (with inventory)
			packet.writeInt(buffer, 0);// item level (without inventory)
			packet.writeLong(buffer, 150);
			packet.writeInt(buffer, 8000);
			packet.writeInt(buffer, 1);

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public UserInfo()
	{
		this.prepare = ByteBuffer.allocate(20480).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.USER_INFO;
	}

	public ByteBuffer getPrepare()
	{
		return prepare;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);

		// получаем промежуточный буффер
		ByteBuffer prepare = getPrepare();

		// переносим данные
		buffer.put(prepare.array(), 0, prepare.limit());
	}
}