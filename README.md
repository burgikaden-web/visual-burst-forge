# Visual Burst — Forge 1.20.1

A Minecraft Java Edition Forge mod that adds the **Burst Core**, a reusable visual-effects item.

## Features
- Right-click to create a burst of vanilla explosion, flame, cloud, and end-rod particles in front of the player.
- Short cooldown.
- Server-side particle spawning for multiplayer compatibility.

## Effect scope
The Burst Core is visual-only. It does not create a Minecraft Explosion, damage entities, ignite blocks, or destroy terrain.

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
2. Put `visualburst-1.0.0.jar` from `build/libs/` into your Minecraft `mods` folder.
3. Launch the Forge profile.
4. Find **Burst Core** in Tools & Utilities, or use `/give @p visualburst:burst_core`.

GitHub Actions also builds the mod on pushes and pull requests. Download the compiled JAR from the workflow run's **Artifacts** section.

## License
MIT
