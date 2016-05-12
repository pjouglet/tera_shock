package tera.gameserver.model.items;

import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.WeaponTemplate;

/**
 * Модель оружия.
 * 
 * @author Ronn
 */
public final class WeaponInstance extends GearedInstance
{
	/** атака оружия */
	private int attack;
	/** сила оружия */
	private int impact;

	/**
	 * @param objectId уник ид оружия.
	 * @param template темплейт оружия.
	 */
	public WeaponInstance(int objectId, ItemTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkCrystal(CrystalInstance crystal)
	{
		if (crystals == null || crystal.getType() != CrystalType.WEAPON)
			return false;

		if (crystal.getItemLevel() > template.getItemLevel())
			return false;

		return crystals.hasEmptySlot();
	}

	@Override
	public WeaponTemplate getTemplate()
	{
		return (WeaponTemplate) template;
	}

	@Override
	public WeaponInstance getWeapon()
	{
		return this;
	}

	@Override
	public boolean isWeapon()
	{
		return true;
	}

	@Override
	protected void updateEnchantStats()
	{
		int attack = super.getAttack();
		int impact = super.getImpact();

		float mod = 3F * getEnchantLevel() / 100F + 1;

		attack *= mod;

		mod = 7F * getEnchantLevel() / 100F + 1;

		impact *= mod;

		setAttack(attack);
		setImpact(impact);
	}

	private void setAttack(int attack)
	{
		this.attack = attack;
	}

	private void setImpact(int impact)
	{
		this.impact = impact;
	}

	@Override
	public int getImpact()
	{
		return impact;
	}

	@Override
	public int getAttack()
	{
		return attack;
	}
}