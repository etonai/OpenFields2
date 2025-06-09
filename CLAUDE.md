# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenFields2 is a Java-based tactical combat simulation game built with JavaFX. It implements a real-time strategy game where units can move, attack, and be damaged in a turn-based system with scheduled events.

## Key Architecture

- **Single-file structure**: All core classes are in `src/main/java/OpenFields2.java`
- **Event-driven system**: Uses `ScheduledEvent` with `PriorityQueue` for delayed actions (attacks, effects)
- **Game loop**: 60 FPS JavaFX Timeline driving tick-based simulation
- **Core entities**: `Character`, `Unit`, `Weapon`, `WeaponState` classes
- **Coordinate system**: 7 pixels = 1 foot conversion for distance calculations

## Development Commands

### Build and Test
```bash
mvn compile                    # Compile the project
mvn test                      # Run all tests
mvn test -Dtest=ClassName     # Run specific test class
mvn clean                     # Clean build artifacts
```

### Run Application
```bash
mvn javafx:run                # Run the JavaFX application
```

## Game Mechanics

- **Movement**: Right-click empty space to move selected unit
- **Movement Types**: W/S keys to increase/decrease movement speed (Crawl, Walk, Jog, Run)
- **Aiming Speed**: Q/E keys to increase/decrease aiming speed (Careful, Normal, Quick)
- **Combat**: Right-click enemy unit to schedule ranged attack
- **Selection**: Left-click unit to select
- **Controls**: Arrow keys pan, +/- zoom, Space pauses/resumes
- **Health system**: Units become incapacitated at 0 health, movement stops
- **Attack timing**: Projectile travel time based on weapon velocity and distance

### Movement System
- **Crawl**: 0.25x base speed (10.5 pixels/sec)
- **Walk**: 1.0x base speed (42 pixels/sec) - default
- **Jog**: 1.5x base speed (63 pixels/sec)
- **Run**: 2.0x base speed (84 pixels/sec)
- **Controls**: W to increase, S to decrease movement type
- **Display**: Selected unit shows current movement type below name

### Aiming Speed System
- **Careful**: 2.0x slower aiming, +15 accuracy bonus - deliberate aimed shots
- **Normal**: 1.0x baseline aiming timing, no accuracy modifier - standard aiming
- **Quick**: 0.5x faster aiming, -20 accuracy penalty - rapid aiming
- **Controls**: Q to increase, E to decrease aiming speed
- **Display**: Selected unit shows current aiming speed below movement type
- **Timing**: Only affects aiming phase duration, not other weapon states

### Combat Modifiers
- **Movement Penalties**: Shooting accuracy decreases while moving
  - **Walking**: -5 modifier
  - **Crawling**: -10 modifier
  - **Jogging**: -15 modifier
  - **Running**: -25 modifier
  - **Stationary**: No penalty (0 modifier)
- **Aiming Speed Modifiers**: Accuracy affected by aiming technique
  - **Careful**: +15 accuracy modifier (deliberate aiming)
  - **Normal**: 0 accuracy modifier (standard aiming)
  - **Quick**: -20 accuracy modifier (rapid aiming)
- **Incapacitated units**: Considered stationary (no movement penalty), cannot change aiming speed

## Technical Details

- **Java 21** with JavaFX 21.0.2
- **JUnit 5** for testing
- **Maven** build system
- **Tick-based timing**: 60 ticks per second, events scheduled by tick number
- **Canvas rendering**: Custom 2D graphics with zoom/pan support