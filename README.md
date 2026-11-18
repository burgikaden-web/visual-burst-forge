# Visual Burst — Forge 1.20.1

A Minecraft Java Edition Forge mod with three arcade-style burst abilities.

## Abilities
- **Burst Core** — aim and right-click to create a forward burst. Nearby mobs receive a short radial knockback.
- **Shockwave Core** — right-click to send a wide ground pulse that pushes nearby mobs outward.
- **Launch Core** — right-click to boost yourself upward and in the direction you're looking.

All abilities have cooldowns and use built-in Minecraft particles and sounds.

## Effect scope
These are game-style movement effects. They do not create a Minecraft Explosion, damage entities, ignite blocks, or destroy terrain. The knockback affects nearby mobs, not other players.

## Requirements
- Minecraft Java Edition 1.20.1
- Minecraft Forge 47.2.0 (Forge 47.x)
- Java 17

## Build
Run using Java 17 and Gradle 8.1.1:

```bash
gradle build
```

The JAR will be in `build/libs/`.

## Install
1. Install Forge for Minecraft 1.20.1.
2. Put the compiled `visualburst-1.0.0.jar` from `build/libs/` into your Minecraft `mods` folder.
3. Launch the Forge profile.
4. Find the abilities in Tools & Utilities or use:
   - `/give @p visualburst:burst_core`
   - `/give @p visualburst:shockwave_core`
   - `/give @p visualburst:launch_core`

GitHub Actions builds the mod on pushes and pull requests. Download the compiled JAR from the workflow run's **Artifacts** section.

## License
MIT
