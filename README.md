# Parry Mod (Minecraft 1.20.1, Forge 47.3.0)

Press **F** to parry (rebind in Options > Controls > Parry).

- Window: 8 ticks (0.4s) after pressing; miss = 10s cooldown, success = 0.25s cooldown.
- Melee hit during the window: damage cancelled, attacker knocked back.
- Projectile (arrow etc.) during the window: deflected back where you look.
- Sword icon + countdown (10, 9, 8...) under the crosshair while on cooldown.
- Plays `parry.ogg` + spark particles.
- Window, success cooldown and knockback are in `config/parry-common.toml`. Miss cooldown is fixed at 10s.

## Build (needs JDK 17)
    gradle build           # (Gradle 8.1.1, or use the wrapper from the official Forge 1.20.1 MDK) jar -> build/libs/parrymod-1.0.0.jar
    gradle runClient    # test in dev
Drop the jar into `mods/` of a Forge 1.20.1 install.

Note: vanilla "Swap hands" is also F; the mod suppresses it when F is pressed.
Both will show red in Controls until you rebind one.
