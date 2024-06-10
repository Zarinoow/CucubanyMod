package fr.cucubany.cucubanymod.roleplay;

import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;

public class Identity {
    private final String firstName;
    private final String lastName;

    private final PlayerSkill education = new PlayerSkill();

    public Identity(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public PlayerSkill getEducation() {
        return this.education;
    }
}
