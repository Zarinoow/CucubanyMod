package fr.cucubany.cucubanymod.roleplay.education;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerSkill {

    private final Set<Skill> skills = new LinkedHashSet<>();

    public PlayerSkill() {
        skills.add(new Skill.OutdoorSurvival());
        skills.add(new Skill.Engineering());
        skills.add(new Skill.Medicine());
        skills.add(new Skill.Cooking());
        skills.add(new Skill.Building());
        skills.add(new Skill.Defense());
    }

    public Set<Skill> getSkills() {
        return skills;
    }

}
