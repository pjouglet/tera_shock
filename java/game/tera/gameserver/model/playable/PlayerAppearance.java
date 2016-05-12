package tera.gameserver.model.playable;

import java.lang.reflect.Field;

import rlib.logging.Loggers;
import rlib.util.ReflectionUtils;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * Модель описания внешности игрока.
 *
 * @author Ronn
 */
public class PlayerAppearance implements Foldable, Cloneable
{
	private static final FoldablePool<PlayerAppearance> pool = Pools.newConcurrentFoldablePool(PlayerAppearance.class);

	public static final PlayerAppearance getInstance(int objectId)
	{
		PlayerAppearance appearance = pool.take();

		if(appearance == null)
			appearance = new PlayerAppearance();

		appearance.setObjectId(objectId);

		return appearance;
	}

	/** уникальный ид игрока */
	private int objectId;

	/** параметры внешности */
	private int faceColor;
	private int faceSkin;
	private int adormentsSkin;
	private int featuresSkin;
	private int featuresColor;
	private int voice;
	private int boneStructureBrow;
	private int boneStructureCheekbones;
	private int boneStructureJaw;
	private int boneStructureJawJut;
	private int earsRotation;
	private int earsExtension;
	private int earsTrim;
	private int earsSize;
	private int eyesWidth;
	private int eyesHeight;
	private int eyesSeparation;
	private int eyesAngle;
	private int eyesInnerBrow;
	private int eyesOuterBrow;
	private int noseExtension;
	private int noseSize;
	private int noseBridge;
	private int noseNostrilWidth;
	private int noseTipWidth;
	private int noseTip;
	private int noseNostrilFlare;
	private int mouthPucker;
	private int mouthPosition;
	private int mouthWidth;
	private int mouthLipThickness;
	private int mouthCorners;
	private int eyesShape;
	private int noseBend;
	private int boneStructureJawWidth;
	private int mothGape;

	/**
	 * @return objectId
	 */
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * @param objectId задаваемое objectId
	 */
	public final void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @return faceColor
	 */
	public final int getFaceColor()
	{
		return faceColor;
	}

	/**
	 * @param faceColor задаваемое faceColor
	 */
	public final void setFaceColor(int faceColor)
	{
		this.faceColor = faceColor;
	}

	/**
	 * @return faceSkin
	 */
	public final int getFaceSkin()
	{
		return faceSkin;
	}

	/**
	 * @param faceSkin задаваемое faceSkin
	 */
	public final void setFaceSkin(int faceSkin)
	{
		this.faceSkin = faceSkin;
	}
	/**
	 * @return adormentsSkin
	 */
	public final int getAdormentsSkin()
	{
		return adormentsSkin;
	}

	/**
	 * @param adormentsSkin задаваемое adormentsSkin
	 */
	public final void setAdormentsSkin(int adormentsSkin)
	{
		this.adormentsSkin = adormentsSkin;
	}

	/**
	 * @return featuresSkin
	 */
	public final int getFeaturesSkin()
	{
		return featuresSkin;
	}

	/**
	 * @param featuresSkin задаваемое featuresSkin
	 */
	public final void setFeaturesSkin(int featuresSkin)
	{
		this.featuresSkin = featuresSkin;
	}

	/**
	 * @return featuresColor
	 */
	public final int getFeaturesColor()
	{
		return featuresColor;
	}

	/**
	 * @param featuresColor задаваемое featuresColor
	 */
	public final void setFeaturesColor(int featuresColor)
	{
		this.featuresColor = featuresColor;
	}

	/**
	 * @return voice
	 */
	public final int getVoice()
	{
		return voice;
	}

	/**
	 * @param voice задаваемое voice
	 */
	public final void setVoice(int voice)
	{
		this.voice = voice;
	}

	/**
	 * @return boneStructureBrow
	 */
	public final int getBoneStructureBrow()
	{
		return boneStructureBrow;
	}

	/**
	 * @param boneStructureBrow задаваемое boneStructureBrow
	 */
	public final void setBoneStructureBrow(int boneStructureBrow)
	{
		this.boneStructureBrow = boneStructureBrow;
	}

	/**
	 * @return boneStructureCheekbones
	 */
	public final int getBoneStructureCheekbones()
	{
		return boneStructureCheekbones;
	}

	/**
	 * @param boneStructureCheekbones задаваемое boneStructureCheekbones
	 */
	public final void setBoneStructureCheekbones(int boneStructureCheekbones)
	{
		this.boneStructureCheekbones = boneStructureCheekbones;
	}

	/**
	 * @return boneStructureJaw
	 */
	public final int getBoneStructureJaw()
	{
		return boneStructureJaw;
	}

	/**
	 * @param boneStructureJaw задаваемое boneStructureJaw
	 */
	public final void setBoneStructureJaw(int boneStructureJaw)
	{
		this.boneStructureJaw = boneStructureJaw;
	}

	/**
	 * @return boneStructureJawJut
	 */
	public final int getBoneStructureJawJut()
	{
		return boneStructureJawJut;
	}

	/**
	 * @param boneStructureJawJut задаваемое boneStructureJawJut
	 */
	public final void setBoneStructureJawJut(int boneStructureJawJut)
	{
		this.boneStructureJawJut = boneStructureJawJut;
	}

	/**
	 * @return earsRotation
	 */
	public final int getEarsRotation()
	{
		return earsRotation;
	}

	/**
	 * @param earsRotation задаваемое earsRotation
	 */
	public final void setEarsRotation(int earsRotation)
	{
		this.earsRotation = earsRotation;
	}

	/**
	 * @return earsExtension
	 */
	public final int getEarsExtension()
	{
		return earsExtension;
	}

	/**
	 * @param earsExtension задаваемое earsExtension
	 */
	public final void setEarsExtension(int earsExtension)
	{
		this.earsExtension = earsExtension;
	}

	/**
	 * @return earsTrim
	 */
	public final int getEarsTrim()
	{
		return earsTrim;
	}

	/**
	 * @param earsTrim задаваемое earsTrim
	 */
	public final void setEarsTrim(int earsTrim)
	{
		this.earsTrim = earsTrim;
	}

	/**
	 * @return earsSize
	 */
	public final int getEarsSize()
	{
		return earsSize;
	}

	/**
	 * @param earsSize задаваемое earsSize
	 */
	public final void setEarsSize(int earsSize)
	{
		this.earsSize = earsSize;
	}

	/**
	 * @return eyesWidth
	 */
	public final int getEyesWidth()
	{
		return eyesWidth;
	}

	/**
	 * @param eyesWidth задаваемое eyesWidth
	 */
	public final void setEyesWidth(int eyesWidth)
	{
		this.eyesWidth = eyesWidth;
	}

	/**
	 * @return eyesHeight
	 */
	public final int getEyesHeight()
	{
		return eyesHeight;
	}

	/**
	 * @param eyesHeight задаваемое eyesHeight
	 */
	public final void setEyesHeight(int eyesHeight)
	{
		this.eyesHeight = eyesHeight;
	}

	/**
	 * @return eyesSeparation
	 */
	public final int getEyesSeparation()
	{
		return eyesSeparation;
	}
	/**
	 * @param eyesSeparation задаваемое eyesSeparation
	 */
	public final void setEyesSeparation(int eyesSeparation)
	{
		this.eyesSeparation = eyesSeparation;
	}

	/**
	 * @return eyesAngle
	 */
	public final int getEyesAngle()
	{
		return eyesAngle;
	}

	/**
	 * @param eyesAngle задаваемое eyesAngle
	 */
	public final void setEyesAngle(int eyesAngle)
	{
		this.eyesAngle = eyesAngle;
	}

	/**
	 * @return eyesInnerBrow
	 */
	public final int getEyesInnerBrow()
	{
		return eyesInnerBrow;
	}

	/**
	 * @param eyesInnerBrow задаваемое eyesInnerBrow
	 */
	public final void setEyesInnerBrow(int eyesInnerBrow)
	{
		this.eyesInnerBrow = eyesInnerBrow;
	}

	/**
	 * @return eyesOuterBrow
	 */
	public final int getEyesOuterBrow()
	{
		return eyesOuterBrow;
	}

	/**
	 * @param eyesOuterBrow задаваемое eyesOuterBrow
	 */
	public final void setEyesOuterBrow(int eyesOuterBrow)
	{
		this.eyesOuterBrow = eyesOuterBrow;
	}

	/**
	 * @return noseExtension
	 */
	public final int getNoseExtension()
	{
		return noseExtension;
	}

	/**
	 * @param noseExtension задаваемое noseExtension
	 */
	public final void setNoseExtension(int noseExtension)
	{
		this.noseExtension = noseExtension;
	}

	/**
	 * @return noseSize
	 */
	public final int getNoseSize()
	{
		return noseSize;
	}

	/**
	 * @param noseSize задаваемое noseSize
	 */
	public final void setNoseSize(int noseSize)
	{
		this.noseSize = noseSize;
	}

	/**
	 * @return noseBridge
	 */
	public final int getNoseBridge()
	{
		return noseBridge;
	}

	/**
	 * @param noseBridge задаваемое noseBridge
	 */
	public final void setNoseBridge(int noseBridge)
	{
		this.noseBridge = noseBridge;
	}

	/**
	 * @return noseNostrilWidth
	 */
	public final int getNoseNostrilWidth()
	{
		return noseNostrilWidth;
	}

	/**
	 * @param noseNostrilWidth задаваемое noseNostrilWidth
	 */
	public final void setNoseNostrilWidth(int noseNostrilWidth)
	{
		this.noseNostrilWidth = noseNostrilWidth;
	}

	/**
	 * @return noseTipWidth
	 */
	public final int getNoseTipWidth()
	{
		return noseTipWidth;
	}

	/**
	 * @param noseTipWidth задаваемое noseTipWidth
	 */
	public final void setNoseTipWidth(int noseTipWidth)
	{
		this.noseTipWidth = noseTipWidth;
	}

	/**
	 * @return noseTip
	 */
	public final int getNoseTip()
	{
		return noseTip;
	}

	/**
	 * @param noseTip задаваемое noseTip
	 */
	public final void setNoseTip(int noseTip)
	{
		this.noseTip = noseTip;
	}

	/**
	 * @return noseNostrilFlare
	 */
	public final int getNoseNostrilFlare()
	{
		return noseNostrilFlare;
	}

	/**
	 * @param noseNostrilFlare задаваемое noseNostrilFlare
	 */
	public final void setNoseNostrilFlare(int noseNostrilFlare)
	{
		this.noseNostrilFlare = noseNostrilFlare;
	}

	/**
	 * @return mouthPucker
	 */
	public final int getMouthPucker()
	{
		return mouthPucker;
	}

	/**
	 * @param mouthPucker задаваемое mouthPucker
	 */
	public final void setMouthPucker(int mouthPucker)
	{
		this.mouthPucker = mouthPucker;
	}

	/**
	 * @return mouthPosition
	 */
	public final int getMouthPosition()
	{
		return mouthPosition;
	}

	/**
	 * @param mouthPosition задаваемое mouthPosition
	 */
	public final void setMouthPosition(int mouthPosition)
	{
		this.mouthPosition = mouthPosition;
	}

	/**
	 * @return mouthWidth
	 */
	public final int getMouthWidth()
	{
		return mouthWidth;
	}

	/**
	 * @param mouthWidth задаваемое mouthWidth
	 */
	public final void setMouthWidth(int mouthWidth)
	{
		this.mouthWidth = mouthWidth;
	}

	/**
	 * @return mouthLipThickness
	 */
	public final int getMouthLipThickness()
	{
		return mouthLipThickness;
	}

	/**
	 * @param mouthLipThickness задаваемое mouthLipThickness
	 */
	public final void setMouthLipThickness(int mouthLipThickness)
	{
		this.mouthLipThickness = mouthLipThickness;
	}

	/**
	 * @return mouthCorners
	 */
	public final int getMouthCorners()
	{
		return mouthCorners;
	}

	/**
	 * @param mouthCorners задаваемое mouthCorners
	 */
	public final void setMouthCorners(int mouthCorners)
	{
		this.mouthCorners = mouthCorners;
	}

	/**
	 * @return eyesShape
	 */
	public final int getEyesShape()
	{
		return eyesShape;
	}

	/**
	 * @param eyesShape задаваемое eyesShape
	 */
	public final void setEyesShape(int eyesShape)
	{
		this.eyesShape = eyesShape;
	}

	/**
	 * @return noseBend
	 */
	public final int getNoseBend()
	{
		return noseBend;
	}

	/**
	 * @param noseBend задаваемое noseBend
	 */
	public final void setNoseBend(int noseBend)
	{
		this.noseBend = noseBend;
	}

	/**
	 * @return boneStructureJawWidth
	 */
	public final int getBoneStructureJawWidth()
	{
		return boneStructureJawWidth;
	}

	/**
	 * @param boneStructureJawWidth задаваемое boneStructureJawWidth
	 */
	public final void setBoneStructureJawWidth(int boneStructureJawWidth)
	{
		this.boneStructureJawWidth = boneStructureJawWidth;
	}

	/**
	 * @return mothGape
	 */
	public final int getMothGape()
	{
		return mothGape;
	}

	/**
	 * @param mothGape задаваемое mothGape
	 */
	public final void setMothGape(int mothGape)
	{
		this.mothGape = mothGape;
	}

	@Override
	public void finalyze(){}

	@Override
	public void reinit(){}

	public void fold()
	{
		pool.put(this);
	}

	public static String toXML(PlayerAppearance appearance, String id)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<appearance id=\"").append(id).append("\" >\n");

		Array<Field> fields = ReflectionUtils.getAllFields(appearance.getClass(), Object.class, true, "pool", "objectId");

		try
		{
			for(Field field : fields)
			{
				String name = field.getName();

				boolean old = field.isAccessible();

				field.setAccessible(true);

				String value = String.valueOf(field.get(appearance));

				builder.append("	<set name=\"").append(name).append("\" value=\"")
				.append(value).append("\" />").append("\n");

				field.setAccessible(old);
			}
		}
		catch(IllegalArgumentException | IllegalAccessException e)
		{
			Loggers.warning(appearance.getClass(), e);
		}

		builder.append("</appearance>");

		return builder.toString();
	}

	/**
	 * Сформировать внешность по данным с хмл.
	 *
	 * @param appearance внешность, в которую вносить данные.
	 * @param vars таблица параметров.
	 * @return новая внешность.
	 */
	public static <T extends PlayerAppearance> T fromXML(T appearance, VarTable vars)
	{
		Array<Field> fields = ReflectionUtils.getAllFields(appearance.getClass(), Object.class, true, "pool", "objectId");

		try
		{
			for(Field field : fields)
			{
				boolean old = field.isAccessible();

				field.setAccessible(true);

				field.setInt(appearance, vars.getInteger(field.getName(), field.getInt(appearance)));

				field.setAccessible(old);
			}
		}
		catch(IllegalArgumentException | IllegalAccessException e)
		{
			Loggers.warning(appearance.getClass(), e);
		}

		return appearance;
	}

	/**
	 * @return копирование внешности.
	 */
	public PlayerAppearance copy()
	{
		try
		{
			return (PlayerAppearance) clone();
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
}
