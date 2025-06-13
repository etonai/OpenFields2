# OpenFields2 - AI-Assisted Tactical Combat Simulation

## Project Vision

OpenFields2 brings the tactical depth of army men toy battles to digital life. This JavaFX-based real-time strategy game simulates detailed combat scenarios where individual soldiers move, aim, and fight with realistic timing and physics.

## The AI Coding Journey

A few years ago, I attempted to build this tactical combat simulation manually - a complex undertaking that proved challenging to complete alone. The vision was clear: create a game where plastic army men could engage in realistic tactical combat with individual unit control, physics-based projectiles, and detailed stat systems.

Recently, I decided to experiment with AI coding assistants to tackle this ambitious project. After three days working with ChatGPT, I discovered Claude Code and was impressed by its ability to handle complex codebases and maintain context across thousands of lines of code. The transition to Claude Code Pro has enabled rapid development of sophisticated game mechanics that would have taken months to implement manually.

This project represents a collaboration between human game design vision and AI-assisted implementation, demonstrating how AI tools can help transform ambitious ideas into working software.

## Game Features

### Tactical Combat System
- **Individual Unit Control**: Select and command individual soldiers with unique stats and abilities
- **Realistic Physics**: Projectile travel time, weapon velocity, and distance-based accuracy
- **Stat-Based Mechanics**: Dexterity, Strength, Reflexes, Health, and Coolness affect combat performance
- **Skill Progression**: Pistol, Rifle, Quickdraw, and Medicine skills influence effectiveness

### Movement and Positioning
- **Variable Movement Speeds**: Crawl, Walk, Jog, and Run with accuracy penalties
- **Aiming Techniques**: Careful, Normal, and Quick aiming with speed/accuracy tradeoffs
- **Real-Time Combat**: 60 FPS game loop with scheduled events for realistic timing

### Weapon Systems
- **Multiple Weapon Types**: Pistols, rifles, and submachine guns with unique characteristics
- **Firing Modes**: Single shot, burst fire, and automatic firing capabilities
- **Ammunition Management**: Realistic reload times and ammunition tracking
- **Automatic Targeting**: Optional AI-assisted target acquisition

## Getting Started

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+** for build management
- **JavaFX 21.0.2** (handled automatically by Maven)

### Development Setup
1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd OF2Prototype01
   ```

2. **IDE Configuration**
   - **IntelliJ IDEA**: Import as Maven project, ensure Project SDK is Java 21
   - **Eclipse**: Import as Existing Maven Project, configure Java 21 JRE
   - **VS Code**: Install Java Extension Pack, ensure JAVA_HOME points to Java 21

3. **Build and Run**
   ```bash
   # Compile the project
   mvn compile
   
   # Run the application
   mvn javafx:run
   ```

### Game Controls
- **Movement**: Right-click empty space to move selected unit
- **Combat**: Right-click enemy unit to attack
- **Selection**: Left-click unit to select
- **Movement Speed**: W/S keys (Crawl, Walk, Jog, Run)
- **Aiming Speed**: Q/E keys (Careful, Normal, Quick)
- **Camera**: Arrow keys to pan, +/- to zoom
- **Pause**: Space bar to pause/resume simulation

## Technical Architecture

### Core Components
- **Single-File Design**: Core game logic in `src/main/java/OpenFields2.java`
- **Event-Driven System**: Scheduled events with priority queue for delayed actions
- **Entity Framework**: Character, Unit, Weapon, and WeaponState classes
- **Rendering System**: Custom 2D graphics with zoom/pan camera support

### Coordinate System
- **Scale**: 7 pixels = 1 foot for realistic distance calculations
- **Movement**: Pixel-based positioning with foot-based game logic
- **Range Calculation**: Euclidean distance for weapon range and accuracy

### Save System
- **JSON Format**: Jackson-based serialization for game state persistence
- **Character Data**: Stats, skills, equipment, and combat preferences
- **Scenario Support**: Save and load tactical scenarios for testing

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Copyright

Copyright (c) 2025 Edward T. Tonai

---

*OpenFields2 demonstrates the potential of AI-assisted game development, combining traditional game design principles with modern AI coding tools to create engaging tactical combat experiences.*