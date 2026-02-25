package fr.cucubany.cucubanymod.roleplay;

import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;

public class Identity {
    private final String firstName;
    private final String lastName;
    private boolean isSlim;

    private final PlayerSkill education = new PlayerSkill();
    private final GenderOption genderOption = new GenderOption();

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

    public boolean isSlim() { return isSlim; }
    public void setSlim(boolean slim) { isSlim = slim; }

    public GenderOption getGenderOption() { return genderOption; }
}
