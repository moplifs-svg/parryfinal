package com.parrymod;

import net.minecraftforge.common.ForgeConfigSpec;

public class ParryConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue WINDOW_TICKS;
    public static final ForgeConfigSpec.IntValue SUCCESS_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue KNOCKBACK;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("parry");
        WINDOW_TICKS = b.comment("How long the parry window lasts after pressing the key (20 ticks = 1 second)")
                .defineInRange("windowTicks", 8, 1, 40);
        SUCCESS_COOLDOWN_TICKS = b.comment("Shorter cooldown after a successful parry")
                .defineInRange("successCooldownTicks", 5, 0, 200);
        KNOCKBACK = b.comment("How hard a parried melee attacker is knocked back")
                .defineInRange("knockback", 0.8D, 0.0D, 5.0D);
        b.pop();
        SPEC = b.build();
    }
}
