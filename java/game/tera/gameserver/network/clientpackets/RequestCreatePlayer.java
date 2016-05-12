package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.PlayerManager;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.network.serverpackets.CreatePlayerResult;

/**
 * Клиентский пакет для создания персонажа
 *
 * @author Ronn
 */
public class RequestCreatePlayer extends ClientPacket
{
	/** имя игрока */
	private String name;

	/** внешность игрока */
	private PlayerAppearance appearance;

	/** пол игрока */
	private Sex sex;
	/** раса игрока */
	private Race race;
	/** класс игрока */
	private PlayerClass playerClass;

	@Override
	public void finalyze()
	{
		name = null;
		appearance = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		appearance = PlayerAppearance.getInstance(0);

		readInt(); // не известно, всегда 1f002d00
		readShort(); // не известно, всегда 2000

		sex = Sex.valueOf(readByte()); // пол, 00 - муж., 01 - жен.

		readByte();//face.tempVals[0] = readByte(); // не известно
		readByte(); //face.tempVals[1] = readByte(); // не известно
		readByte(); //face.tempVals[2] = readByte(); // не известно

		race = Race.valueOf(readByte(), sex); // ID расы...Человек 00, Эльфы 01, Аман 02, Кастаник 03, Попори и Елин 04, Барака 05

		readByte(); //face.tempVals[3] = readByte(); // не известно
		readByte(); //face.tempVals[4] = readByte(); // не известно
		readByte(); //face.tempVals[5] = readByte(); // не известно

		playerClass = PlayerClass.values()[readByte()]; // класс

		readByte(); //face.tempVals[6] = readByte();
		readByte(); //face.tempVals[7] = readByte();
		readByte(); //face.tempVals[8] = readByte();
		readByte(); //face.tempVals[9] = readByte();

		appearance.setFaceColor(readByte()); //face.setFaceColor(readByte()); // цвет лица

		appearance.setFaceSkin(readByte()); //face.tempVals[10] = readByte();
		appearance.setAdormentsSkin(readByte()); //face.tempVals[11] = readByte();
		appearance.setFeaturesSkin(readByte()); //face.tempVals[12] = readByte();

		appearance.setFeaturesColor(readByte()); //face.setHairColor(readByte());  // цвет волос

		appearance.setVoice(readByte()); //face.tempVals[13] = readByte();

		readByte(); //face.tempVals[14] = readByte(); //голос
		readByte();

		name = readString();

		//buffer.position(buffer.position() - 1);

		//readByte();  //face.tempVals[15] = readByte();

		appearance.setBoneStructureBrow(readByte()); //face.setEyebrowsFirstVal(readByte()); // 3 - брови 1
		appearance.setBoneStructureCheekbones(readByte()); //face.setEyeFirstVal(readByte()); // 4 - под глазами херь...
		appearance.setBoneStructureJaw(readByte()); //face.setChin(readByte()); // 5 - подбородок
		appearance.setBoneStructureJawJut(readByte()); //face.setCheekbonePos(readByte()); // 7 - скулы вперед-назад...

		appearance.setEarsRotation(readByte()); //face.setEarsFirstVal(readByte()); // 29 - уши 1
		appearance.setEarsExtension(readByte()); //face.setEarsSecondVal(readByte()); // 30 - уши 2
		appearance.setEarsTrim(readByte()); //face.setEarsThridVal(readByte()); // 31 - уши 3
		appearance.setEarsSize(readByte()); //face.setEarsFourthVal(readByte()); // 32 - уши 4

		appearance.setEyesWidth(readByte()); //face.setEyeWidth(readByte()); // 8 - ширина глаз...
		appearance.setEyesHeight(readByte()); //face.setEyePosVertical(readByte()); // 10 - выше-ниже глаза...
		appearance.setEyesSeparation(readByte()); //face.setEyeSecondVal(readByte()); // 11 - опять глаза...

		readByte(); //face.tempVals[16] = readByte(); // не известно...

		appearance.setEyesAngle(readByte()); //face.setEyeThridVal(readByte()); // 12 - тоже глаза...
		appearance.setEyesInnerBrow(readByte()); //face.setEyebrowsSecondVal(readByte()); // 13 - брови 2
		appearance.setEyesOuterBrow(readByte()); //face.setEyebrowsThridVal(readByte()); // 14 - брови 3

		readByte(); //face.tempVals[17] = readByte(); // не известно...

		appearance.setNoseExtension(readByte()); //face.setNoseFirstVal(readByte()); // 15 - нос 1
		appearance.setNoseSize(readByte()); //face.setNoseSecondVal(readByte()); // 16 - нос 2
		appearance.setNoseBridge(readByte()); //face.setBridgeFirstVal(readByte()); // 17 - переносица
		appearance.setNoseNostrilWidth(readByte()); //face.setBridgeSecondVal(readByte()); // 18 - переносица 2
		appearance.setNoseTipWidth(readByte()); //face.setBridgeThridVal(readByte()); // 19 - переносица 3
		appearance.setNoseTip(readByte()); //face.setNoseThridVal(readByte()); // 20 - нос 3
		appearance.setNoseNostrilFlare(readByte()); //face.setNoseFourthVal(readByte()); // 21 - нос 4

		appearance.setMouthPucker(readByte()); //face.setLipsFirstVal(readByte()); // 27 - губы 5
		appearance.setMouthPosition(readByte()); //face.setLipsSecondVal(readByte()); // 23 - губы 1
		appearance.setMouthWidth(readByte()); //face.setLipsThridVal(readByte());  // 24 - губы 2
		appearance.setMouthLipThickness(readByte()); //face.setLipsFourthVal(readByte()); // 25 - губы 3
		appearance.setMouthCorners(readByte()); //face.setLipsFifthVal(readByte()); // 28 - губы 6

		appearance.setEyesShape(readByte()); //face.setEyeHeight(readByte()); // 9 - высота глаз...
		appearance.setNoseBend(readByte()); //face.setNoseFifthVal(readByte()); // 22 - нос 5
		appearance.setBoneStructureJawWidth(readByte()); //face.setCheeks(readByte()); // 6 - скулы(щеки)
		appearance.setMothGape(readByte()); //face.setLipsSixthVal(readByte()); // 26 - губы 4
    }

	@Override
	public void runImpl()
	{
		if(name == null)
			return;

		// получаем менеджера игроков
		PlayerManager playerManager = PlayerManager.getInstance();

		// пробуем создать игрока
		playerManager.createPlayer(getOwner(), appearance, name, playerClass, race, sex);

		//System.out.println("appearance " + appearance.toXML(appearance, ""));
		//owner.sendPacket(CreatePlayerResult.getInstance(), true);
	}
}