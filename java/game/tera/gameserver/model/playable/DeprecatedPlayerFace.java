package tera.gameserver.model.playable;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * @author Ronn
 */
public final class DeprecatedPlayerFace implements Foldable
{
	/** пул внешностей игроков */
	private static final FoldablePool<DeprecatedPlayerFace> pool = Pools.newConcurrentFoldablePool(DeprecatedPlayerFace.class);

	/**
	 * Создание новой внешности игрока.
	 *
	 * @param objectId ид игрока.
	 * @return новая внешность.
	 */
	public static final DeprecatedPlayerFace newInstance(int objectId)
	{
		DeprecatedPlayerFace face = pool.take();

		if(face == null)
			face = new DeprecatedPlayerFace(objectId);
		else
			face.objectId =objectId;

		return face;
	}

	private int faceColor;
	private int hairColor;
	private int eyebrowsFirstVal;
	private int eyebrowsSecondVal;
	private int eyebrowsThridVal;
	private int eyeFirstVal;
	private int eyeSecondVal;
	private int eyeThridVal;
	private int eyePosVertical;
	private int eyeWidth;
	private int eyeHeight;
	private int chin;
	private int cheekbonePos;
	private int earsFirstVal;
	private int earsSecondVal;
	private int earsThridVal;
	private int earsFourthVal;
	private int noseFirstVal;
	private int noseSecondVal;
	private int noseThridVal;
	private int noseFourthVal;
	private int noseFifthVal;
	private int lipsFirstVal;
	private int lipsSecondVal;
	private int lipsThridVal;
	private int lipsFourthVal;
	private int lipsFifthVal;
	private int lipsSixthVal;
	private int cheeks;
	private int bridgeFirstVal;
	private int bridgeSecondVal;
	private int bridgeThridVal;

	private int objectId;

	public int[] tempVals;

	/**
	 * @param objectId
	 */
	public DeprecatedPlayerFace(int objectId)
	{
		this.objectId = objectId;
		this.tempVals = new int[19];
	}

	@Override
	public void finalyze()
	{
		faceColor = 0;
		bridgeSecondVal = 0;
		bridgeFirstVal = 0;
		bridgeThridVal = 0;
		cheekbonePos = 0;
		cheeks = 0;
		chin = 0;
		earsFirstVal = 0;
		earsFourthVal = 0;
		earsSecondVal = 0;
		earsThridVal = 0;
		eyebrowsFirstVal = 0;
		eyebrowsSecondVal = 0;
		eyebrowsThridVal = 0;
		eyeFirstVal = 0;
		eyeHeight = 0;
		eyeWidth = 0;
		eyeThridVal = 0;
		faceColor = 0;
		hairColor = 0;
	}

	/**
	 * Сложить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return the bridgeFirstVal
	 */
	public int getBridgeFirstVal()
	{
		return bridgeFirstVal;
	}

	/**
	 * @return the bridgeSecondVal
	 */
	public int getBridgeSecondVal()
	{
		return bridgeSecondVal;
	}

	/**
	 * @return the bridgeThridVal
	 */
	public int getBridgeThridVal()
	{
		return bridgeThridVal;
	}

	/**
	 * @return the cheekbonePos
	 */
	public int getCheekbonePos()
	{
		return cheekbonePos;
	}

	/**
	 * @return the cheeks
	 */
	public int getCheeks()
	{
		return cheeks;
	}

	/**
	 * @return the chin
	 */
	public int getChin()
	{
		return chin;
	}

	/**
	 * @return the earsFirstVal
	 */
	public int getEarsFirstVal()
	{
		return earsFirstVal;
	}

	/**
	 * @return the earsFourthVal
	 */
	public int getEarsFourthVal()
	{
		return earsFourthVal;
	}

	/**
	 * @return the earsSecondVal
	 */
	public int getEarsSecondVal()
	{
		return earsSecondVal;
	}

	/**
	 * @return the earsThridVal
	 */
	public int getEarsThridVal()
	{
		return earsThridVal;
	}

	/**
	 * @return the eyebrows
	 */
	public int getEyebrowsFirstVal()
	{
		return eyebrowsFirstVal;
	}

	/**
	 * @return the eyebrowsSecondVal
	 */
	public int getEyebrowsSecondVal()
	{
		return eyebrowsSecondVal;
	}

	/**
	 * @return the eyebrowsThridVal
	 */
	public int getEyebrowsThridVal()
	{
		return eyebrowsThridVal;
	}

	/**
	 * @return the eyeFirstVal
	 */
	public int getEyeFirstVal()
	{
		return eyeFirstVal;
	}

	/**
	 * @return the eyeHeight
	 */
	public int getEyeHeight()
	{
		return eyeHeight;
	}

	/**
	 * @return the eyePosVertical
	 */
	public int getEyePosVertical()
	{
		return eyePosVertical;
	}

	/**
	 * @return the eyeSecondVal
	 */
	public int getEyeSecondVal()
	{
		return eyeSecondVal;
	}

	/**
	 * @return the eyeThridVal
	 */
	public int getEyeThridVal()
	{
		return eyeThridVal;
	}

	/**
	 * @return the eyeWidth
	 */
	public int getEyeWidth()
	{
		return eyeWidth;
	}

	/**
	 * @return the faceColor
	 */
	public int getFaceColor()
	{
		return faceColor;
	}

	/**
	 * @return the hairColor
	 */
	public int getHairColor()
	{
		return hairColor;
	}

	/**
	 * @return the lipsFifthVal
	 */
	public int getLipsFifthVal()
	{
		return lipsFifthVal;
	}

	/**
	 * @return the lipsFirstVal
	 */
	public int getLipsFirstVal()
	{
		return lipsFirstVal;
	}

	/**
	 * @return the lipsFourthVal
	 */
	public int getLipsFourthVal()
	{
		return lipsFourthVal;
	}

	/**
	 * @return the lipsSecondVal
	 */
	public int getLipsSecondVal()
	{
		return lipsSecondVal;
	}

	/**
	 * @return the lipsSixthVal
	 */
	public int getLipsSixthVal()
	{
		return lipsSixthVal;
	}

	/**
	 * @return the lipsThridVal
	 */
	public int getLipsThridVal()
	{
		return lipsThridVal;
	}

	/**
	 * @return the noseFifthVal
	 */
	public int getNoseFifthVal()
	{
		return noseFifthVal;
	}

	/**
	 * @return the noseFirstVal
	 */
	public int getNoseFirstVal()
	{
		return noseFirstVal;
	}

	/**
	 * @return the noseFourthVal
	 */
	public int getNoseFourthVal()
	{
		return noseFourthVal;
	}

	/**
	 * @return the noseSecondVal
	 */
	public int getNoseSecondVal()
	{
		return noseSecondVal;
	}

	/**
	 * @return the noseThridVal
	 */
	public int getNoseThridVal()
	{
		return noseThridVal;
	}

	/**
	 * @return the objectId
	 */
	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public void reinit(){}

	/**
	 * @param bridgeFirstVal the bridgeFirstVal to set
	 */
	public void setBridgeFirstVal(int bridgeFirstVal)
	{
		this.bridgeFirstVal = bridgeFirstVal;
	}

	/**
	 * @param bridgeSecondVal the bridgeSecondVal to set
	 */
	public void setBridgeSecondVal(int bridgeSecondVal)
	{
		this.bridgeSecondVal = bridgeSecondVal;
	}

	/**
	 * @param bridgeThridVal the bridgeThridVal to set
	 */
	public void setBridgeThridVal(int bridgeThridVal)
	{
		this.bridgeThridVal = bridgeThridVal;
	}

	/**
	 * @param cheekbonePos the cheekbonePos to set
	 */
	public void setCheekbonePos(int cheekbonePos)
	{
		this.cheekbonePos = cheekbonePos;
	}

	/**
	 * @param cheeks the cheeks to set
	 */
	public void setCheeks(int cheeks)
	{
		this.cheeks = cheeks;
	}

	/**
	 * @param chin the chin to set
	 */
	public void setChin(int chin)
	{
		this.chin = chin;
	}

	/**
	 * @param earsFirstVal the earsFirstVal to set
	 */
	public void setEarsFirstVal(int earsFirstVal)
	{
		this.earsFirstVal = earsFirstVal;
	}

	/**
	 * @param earsFourthVal the earsFourthVal to set
	 */
	public void setEarsFourthVal(int earsFourthVal)
	{
		this.earsFourthVal = earsFourthVal;
	}

	/**
	 * @param earsSecondVal the earsSecondVal to set
	 */
	public void setEarsSecondVal(int earsSecondVal)
	{
		this.earsSecondVal = earsSecondVal;
	}

	/**
	 * @param earsThridVal the earsThridVal to set
	 */
	public void setEarsThridVal(int earsThridVal)
	{
		this.earsThridVal = earsThridVal;
	}

	/**
	 * @param eyebrows the eyebrows to set
	 */
	public void setEyebrowsFirstVal(int eyebrowsFirstVal)
	{
		this.eyebrowsFirstVal = eyebrowsFirstVal;
	}

	/**
	 * @param eyebrowsSecondVal the eyebrowsSecondVal to set
	 */
	public void setEyebrowsSecondVal(int eyebrowsSecondVal)
	{
		this.eyebrowsSecondVal = eyebrowsSecondVal;
	}

	/**
	 * @param eyebrowsThridVal the eyebrowsThridVal to set
	 */
	public void setEyebrowsThridVal(int eyebrowsThridVal)
	{
		this.eyebrowsThridVal = eyebrowsThridVal;
	}

	/**
	 * @param eyeFirstVal the eyeFirstVal to set
	 */
	public void setEyeFirstVal(int eyeFirstVal)
	{
		this.eyeFirstVal = eyeFirstVal;
	}

	/**
	 * @param eyeHeight the eyeHeight to set
	 */
	public void setEyeHeight(int eyeHeight)
	{
		this.eyeHeight = eyeHeight;
	}

	/**
	 * @param eyePosVertical the eyePosVertical to set
	 */
	public void setEyePosVertical(int eyePosVertical)
	{
		this.eyePosVertical = eyePosVertical;
	}

	/**
	 * @param eyeSecondVal the eyeSecondVal to set
	 */
	public void setEyeSecondVal(int eyeSecondVal)
	{
		this.eyeSecondVal = eyeSecondVal;
	}

	/**
	 * @param eyeThridVal the eyeThridVal to set
	 */
	public void setEyeThridVal(int eyeThridVal)
	{
		this.eyeThridVal = eyeThridVal;
	}

	/**
	 * @param eyeWidth the eyeWidth to set
	 */
	public void setEyeWidth(int eyeWidth)
	{
		this.eyeWidth = eyeWidth;
	}

	/**
	 * @param faceColor the faceColor to set
	 */
	public void setFaceColor(int faceColor)
	{
		this.faceColor = faceColor;
	}

	/**
	 * @param hairColor the hairColor to set
	 */
	public void setHairColor(int hairColor)
	{
		this.hairColor = hairColor;
	}

	/**
	 * @param lipsFifthVal the lipsFifthVal to set
	 */
	public void setLipsFifthVal(int lipsFifthVal)
	{
		this.lipsFifthVal = lipsFifthVal;
	}

	/**
	 * @param lipsFirstVal the lipsFirstVal to set
	 */
	public void setLipsFirstVal(int lipsFirstVal)
	{
		this.lipsFirstVal = lipsFirstVal;
	}

	/**
	 * @param lipsFourthVal the lipsFourthVal to set
	 */
	public void setLipsFourthVal(int lipsFourthVal)
	{
		this.lipsFourthVal = lipsFourthVal;
	}

	/**
	 * @param lipsSecondVal the lipsSecondVal to set
	 */
	public void setLipsSecondVal(int lipsSecondVal)
	{
		this.lipsSecondVal = lipsSecondVal;
	}

	/**
	 * @param lipsSixthVal the lipsSixthVal to set
	 */
	public void setLipsSixthVal(int lipsSixthVal)
	{
		this.lipsSixthVal = lipsSixthVal;
	}

	/**
	 * @param lipsThridVal the lipsThridVal to set
	 */
	public void setLipsThridVal(int lipsThridVal)
	{
		this.lipsThridVal = lipsThridVal;
	}

	/**
	 * @param noseFifthVal the noseFifthVal to set
	 */
	public void setNoseFifthVal(int noseFifthVal)
	{
		this.noseFifthVal = noseFifthVal;
	}

	/**
	 * @param noseFirstVal the noseFirstVal to set
	 */
	public void setNoseFirstVal(int noseFirstVal)
	{
		this.noseFirstVal = noseFirstVal;
	}

	/**
	 * @param noseFourthVal the noseFourthVal to set
	 */
	public void setNoseFourthVal(int noseFourthVal)
	{
		this.noseFourthVal = noseFourthVal;
	}

	/**
	 * @param noseSecondVal the noseSecondVal to set
	 */
	public void setNoseSecondVal(int noseSecondVal)
	{
		this.noseSecondVal = noseSecondVal;
	}

	/**
	 * @param noseThridVal the noseThridVal to set
	 */
	public void setNoseThridVal(int noseThridVal)
	{
		this.noseThridVal = noseThridVal;
	}

	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	public PlayerAppearance toAppearance()
	{
		PlayerAppearance appearance = PlayerAppearance.getInstance(objectId);

		appearance.setObjectId(objectId);
		appearance.setFaceColor(faceColor);
		appearance.setFaceSkin(tempVals[10]);
		appearance.setAdormentsSkin(tempVals[11]);
		appearance.setFeaturesSkin(tempVals[12]);
		appearance.setFeaturesColor(hairColor);
		appearance.setVoice(tempVals[13]);
		appearance.setBoneStructureBrow(eyebrowsFirstVal);
		appearance.setBoneStructureCheekbones(eyeFirstVal);
		appearance.setBoneStructureJaw(chin);
		appearance.setBoneStructureJawJut(cheekbonePos);
		appearance.setEarsRotation(earsFirstVal);
		appearance.setEarsExtension(earsSecondVal);
		appearance.setEarsTrim(earsThridVal);
		appearance.setEarsSize(earsFourthVal);
		appearance.setEyesWidth(eyeWidth);
		appearance.setEyesHeight(eyePosVertical);
		appearance.setEyesSeparation(eyeSecondVal);
		appearance.setEyesAngle(eyeThridVal);
		appearance.setEyesInnerBrow(eyebrowsSecondVal);
		appearance.setEyesOuterBrow(eyebrowsThridVal);
		appearance.setNoseExtension(noseFirstVal);
		appearance.setNoseSize(noseSecondVal);
		appearance.setNoseBridge(bridgeFirstVal);
		appearance.setNoseNostrilWidth(bridgeSecondVal);
		appearance.setNoseTipWidth(bridgeThridVal);
		appearance.setNoseTip(noseThridVal);
		appearance.setNoseNostrilFlare(noseFourthVal);
		appearance.setMouthPucker(lipsFirstVal);
		appearance.setMouthPosition(lipsSecondVal);
		appearance.setMouthWidth(lipsThridVal);
		appearance.setMouthLipThickness(lipsFourthVal);
		appearance.setMouthCorners(lipsFifthVal);
		appearance.setEyesShape(eyeHeight);
		appearance.setNoseBend(noseFifthVal);
		appearance.setBoneStructureJawWidth(cheeks);
		appearance.setMothGape(lipsSixthVal);

		return appearance;
	}
}

