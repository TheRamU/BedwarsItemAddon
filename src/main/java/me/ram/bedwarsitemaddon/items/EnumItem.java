package me.ram.bedwarsitemaddon.items;

public enum EnumItem {

    BRIDGE_EGG("BridgeEgg"),
    ENDER_PEARL_CHAIR("EnderPearlChair"),
    FIRE_BALL("FireBall"),
    LIGHT_TNT("LightTNT"),
    PARACHUTE("Parachute"),
    TEAM_IRON_GOLEM("TeamIronGolem"),
    TNT_LAUNCH("TNTLaunch"),
    TRAMPOLINE("Trampoline"),
    WALK_PLATFORM("WalkPlatform"),
    MAGIC_MILK("MagicMilk");

    private final String name;

    EnumItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EnumItem getByName(String n) {
        for (EnumItem type : values()) {
            if (type.getName().equals(n)) {
                return type;
            }
        }
        return null;
    }
}
