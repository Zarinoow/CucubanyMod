package fr.cucubany.cucubanymod.capabilities;

import fr.cucubany.cucubanymod.roleplay.Identity;

public class IdentityCapability implements IIdentityCapability {
    private Identity identity;

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public Identity getIdentity() {
        return this.identity;
    }
}
