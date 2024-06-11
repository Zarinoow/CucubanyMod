package fr.cucubany.cucubanymod.roleplay.education;

import net.minecraft.network.chat.TranslatableComponent;

public abstract class Skill implements ISkill{
    private int level = 0;
    private final String keyName;

    public Skill(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    @Override
    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent("skill.cucubanymod." + getKeyName() + ".name");
    }

    @Override
    public TranslatableComponent getDisplayDescription() {
        return new TranslatableComponent("skill.cucubanymod." + getKeyName() + ".description");
    }

    static class OutdoorSurvival extends Skill {
        public OutdoorSurvival() {
            super("outdoor_survival");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:crowbar";
        }
    }

    static class Engineering extends Skill {
        public Engineering() {
            super("engineering");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:blueprint_rare";
        }

    }

    static class Medicine extends Skill {
        public Medicine() {
            super("medicine");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:syringe_stimulator";
        }
    }

    static class Cooking extends Skill implements IAspiration {
        public Cooking() {
            super("cooking");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:frying_pan";
        }
    }

    static class Building extends Skill {
        public Building() {
            super("building");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:plastic_fence";
        }
    }

    static class Defense extends Skill {
        public Defense() {
            super("defense");
        }

        @Override
        public String getDisplayIcon() {
            return "zombie_extreme:metal_fence";
        }
    }
}
