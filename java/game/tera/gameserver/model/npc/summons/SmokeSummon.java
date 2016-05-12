package tera.gameserver.model.npc.summons;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.StatFunc;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель теневого сумона.
 * 
 * @author Ronn
 */
public class SmokeSummon extends DefaultSummon {

	/** функция лимита хп */
	private final Func maxHpFunc;

	/** максимальне хп сумона */
	private int maxHp;

	public SmokeSummon(int objectId, NpcTemplate template) {
		super(objectId, template);

		maxHpFunc = new StatFunc() {

			@Override
			public void addFuncTo(Character owner) {
				owner.addStatFunc(this);
			}

			@Override
			public float calc(Character attacker, Character attacked, Skill skill, float val) {

				Character owner = getOwner();

				if(owner == null) {
					return val;
				}

				return maxHp;
			}

			@Override
			public int compareTo(StatFunc func) {
				return 0x90 - func.getOrder();
			}

			@Override
			public int getOrder() {
				return 0x90;
			}

			@Override
			public StatType getStat() {
				return StatType.MAX_HP;
			}

			@Override
			public void removeFuncTo(Character owner) {
				owner.removeStatFunc(this);
			}
		};

		maxHpFunc.addFuncTo(this);
	}

	@Override
	public int getTemplateId() {

		Character owner = getOwner();

		if(owner != null) {
			return owner.getTemplateId() * 100;
		}

		return super.getTemplateId();
	}

	@Override
	public void spawnMe() {

		Character owner = getOwner();

		if(owner != null) {
			setMaxHp(Math.max((int) (owner.getMaxHp() * 20 / 100 * 7.5F), 1));
		} else {
			setMaxHp(1);
		}

		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());

		super.spawnMe();
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}
}
