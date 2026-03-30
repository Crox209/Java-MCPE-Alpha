# Minecraft PE - Java Edition

A Java implementation of Minecraft Pocket Edition Alpha, converted from the original C++ codebase.

## Overview

This is a Java-based game engine implementation featuring:
- 3D voxel-based world
- Block system with multiple block types
- Player movement and controls
- World generation
- Isometric 2D rendering (can be extended to 3D OpenGL)
- NBT data format support
- Creative and Survival modes
- Mouse and keyboard controls
- Hostile mobs and passive animals
- Dropped item entities with pickup
- Day/night-biased mob spawning
- Basic hostile and passive mob AI behavior
- Physical item drops from broken blocks and mob loot
- Nearby item attraction and stack merging

## Requirements

- Java 11 or higher (Java 17 recommended)
- Gradle 7.0+ or use the included Gradle wrapper

## Building

### Using Gradle

```bash
cd MinecraftJava
./gradlew clean build
```

### Creating a JAR

```bash
./gradlew jar
```

The compiled JAR will be at `build/libs/mcpe-java-0.1.0.jar`

After standard distribution packaging:

- Linux/macOS scripts: `build/install/mcpe-java/bin/mcpe-java`
- Windows script: `build/install/mcpe-java/bin/mcpe-java.bat`
- Distributions: `build/distributions/mcpe-java-0.1.0.zip` and `build/distributions/mcpe-java-0.1.0.tar`

## Running

### From IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, etc.)
2. Run `MinecraftPE.main()` from `com.minecraft.mcpe.MinecraftPE`

### From Command Line

```bash
./gradlew run
```

Or with a built JAR:

```bash
java -jar build/libs/mcpe-java-0.1.0.jar
```

Installable app layout (cross-platform scripts):

```bash
./gradlew installDist
```

If you run on a server/container without a display, the game automatically starts in headless simulation mode instead of opening a Swing window.

## World Menu

On desktop startup, the game opens a world selection menu with these actions:

- **Play** - load selected world
- **Create** - create a new world file
- **Delete** - delete selected world
- **Import** - choose an `.nbt` world file from your computer and import it
- **Export** - copy selected world to a location you choose (defaults to Desktop when available)

World files are stored in:

- `saves/worlds/*.nbt`

## Platform Support

- Linux: `./gradlew run` or `build/install/mcpe-java/bin/mcpe-java`
- macOS: `./gradlew run` or `build/install/mcpe-java/bin/mcpe-java`
- Windows: `gradlew.bat run` or `build\\install\\mcpe-java\\bin\\mcpe-java.bat`

## Controls

- **W/A/S/D** - Move forward/left/back/right
- **Mouse** - Look around (click and drag)
- **Space** - Jump
- **Shift** - Descend while flying
- **F** - Toggle flight mode
- **C** - Toggle creative mode
- **Left Click** - Break block
- **Right Click** - Place block
- **Middle Click** - Pick targeted block into selected slot
- **1-9 / Mouse Wheel** - Select hotbar slot
- **Left Click (on mob)** - Melee attack
- **F5** - Save world
- **F9** - Load world
- **ESC** - Close game

## Project Structure

```
src/main/java/com/minecraft/mcpe/
├── MinecraftPE.java          # Main game class and entry point
├── block/                    # Block system
│   ├── Block.java           # Individual block
│   └── BlockRegistry.java   # All block types
├── entity/                   # Entities (players, mobs)
│   ├── Entity.java          # Base entity class
│   └── Player.java          # Player implementation
├── world/                    # World management
│   ├── World.java           # World/Level management
│   └── Chunk.java           # 16x16x128 chunk storage
├── nbt/                      # NBT data format
│   ├── Tag.java             # NBT tag types
│   └── NbtIo.java           # NBT file I/O
├── renderer/                 # Game rendering
│   └── GameRenderer.java    # Isometric/3D renderer
├── client/                   # Client-side logic
│   └── InputHandler.java    # Keyboard/mouse input
└── util/                     # Utilities
    └── Vector3f.java        # 3D vector math
```

## Features Implemented

### Core
- ✅ Game loop with 60 FPS target
- ✅ World initialization and chunk system
- ✅ Player entity with movement
- ✅ Input handling (keyboard & mouse)
- ✅ Basic terrain rendering (isometric view)
- ✅ Block system with 100+ block types
- ✅ NBT format support

### Gameplay
- ✅ First-person perspective with mouse look
- ✅ Movement (WASD controls)
- ✅ Jumping
- ✅ Creative mode (flight)
- ✅ Survival mode
- ✅ Block breaking and placing
- ✅ Health system

## Future Enhancements

### Rendering
- [ ] OpenGL-based 3D rendering (LWJGL3)
- [ ] Proper lighting and shadows
- [ ] Particle effects
- [ ] Smooth animations

### Gameplay
- [ ] Mobs/Creatures
- [ ] Mob AI
- [ ] Combat system
- [ ] Inventory system
- [ ] Crafting
- [ ] Tools and weapons

### World
- [ ] Procedural terrain generation (Perlin noise)
- [ ] Biomes
- [ ] Caves and structures
- [ ] Water/Lava physics
- [ ] Weather system

### Networking
- [ ] Multiplayer support
- [ ] Server/Client architecture
- [ ] Network synchronization

### Persistence
- [ ] World save/load
- [ ] Player data persistence
- [ ] Configuration files

### Performance
- [ ] Chunk LOD system
- [ ] Frustum culling
- [ ] Multi-threading for chunk generation

## Development

The project uses standard Java conventions:
- Package structure: `com.minecraft.mcpe.*`
- Naming conventions: CamelCase for classes, camelCase for methods
- Logging: SLF4J + Logback
- Build tool: Gradle

## For Converting C++ Code

The C++ code from the original project can be systematically converted:

1. **Headers (.h)** → **Java interfaces/classes**
2. **Implementation (.cpp)** → **Java methods**
3. **STL containers** (`vector`, `map`, etc.) → **Java Collections**
4. **Memory management** (manual in C++) → **Garbage collection (automatic)**
5. **Platform-specific code** → **Platform-independent Java + platform modules**

Key conversion examples already implemented:
- C++ STL containers → Java HashMap/ArrayList
- NBT tag system (complete)
- Block registry system
- Entity/Player classes
- World/Chunk management

## Dependencies

- **SLF4J & Logback** - Logging framework
- **Gson** - JSON library (for configuration)
- **Netty** - Networking (for future multiplayer)
- **JUnit** - Testing framework (optional)

## License

This is an educational project based on the Minecraft PE Alpha source code.

## Contributing

To extend this project:

1. Convert more C++ components to Java
2. Implement OpenGL rendering
3. Add missing gameplay features
4. Optimize performance

See individual files for more specific implementation notes.
