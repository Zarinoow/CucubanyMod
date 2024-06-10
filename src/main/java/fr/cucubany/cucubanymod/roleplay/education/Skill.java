package fr.cucubany.cucubanymod.roleplay.education;

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

    static class OutdoorSurvival extends Skill {
        public OutdoorSurvival() {
            super("outdoor_survival");
        }
    }

    static class Engineering extends Skill {
        public Engineering() {
            super("engineering");
        }
    }

    static class Medicine extends Skill {
        public Medicine() {
            super("medicine");
        }
    }

    static class Cooking extends Skill {
        public Cooking() {
            super("cooking");
        }
    }

    static class Building extends Skill {
        public Building() {
            super("building");
        }
    }

    static class Defense extends Skill {
        public Defense() {
            super("defense");
        }
    }
}
