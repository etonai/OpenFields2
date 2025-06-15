# DevCycle 2025_0008 Brainstorm - Scenario System Foundation
*Created: June 14, 2025*

## **DevCycle 8 - Character Management Foundation**
**Focus**: Create persistent character creation and storage system for future scenario development

### **Core Systems to Build**
- **Character Creation and Deployment System** (CTRL-C/CTRL-A workflow)
- **Character Persistence System** (characters.json storage and updates)

### **Development Approach**
**Strategy**: Build robust character management foundation
- Focus on character creation, storage, and deployment workflow
- Enable persistent character progression across sessions
- Prepare infrastructure for future large-scale scenarios

## **Key Decisions Made**

### **Foundation Approach** ✅
1. **Character Management Priority** - Build character persistence before scenario complexity
2. **System Scope** - Focus only on character creation and storage systems

### **Technical Strategy** ✅
- **Character Storage**: JSON-based persistent character database
- **Performance**: Simple file-based storage, optimize later if needed
- **Save System**: Focus on character data persistence, not scenario states
- **UI Design**: Clean keyboard shortcuts for character management

### **Design Philosophy** ✅
- **Character Persistence**: Characters exist independently of scenarios
- **Flexible Deployment**: Same characters usable across multiple scenario types
- **Character Naming**: Use existing procedural name generation
- **Battle Statistics**: Prepare foundation for future battle tracking

## **Character Persistence System**

### **Character Storage in characters.json**
Each character record contains:
- **Basic Identity**: Name, faction (Union/Confederate), physical stats
- **Battle Statistics**: Shots fired, wounds inflicted/received, targets incapacitated
- **Battle History**: Number of battles participated in
- **Character Stats**: Dexterity, strength, reflexes, health, coolness, skills
- **No Weapons**: Weapons assigned during scenario setup, not stored permanently

### **Scenario Integration Workflow**
1. **Character Selection**: Choose characters from `characters.json` for scenario
2. **Weapon Assignment**: Arm characters with scenario-appropriate weapons (Brown Bess, etc.)
3. **Battle Execution**: Characters fight with assigned equipment
4. **Statistics Update**: Post-battle, update character combat stats in `characters.json`
5. **Weapon Cleanup**: Remove scenario weapons, preserve character progression

## **Open Questions for Further Discussion**

### **Battle Setup Questions** ✅
1. **Battle Size Progression**: Start with 10v10, then scale up
2. **Formation Templates**: Start with simple line formations
3. **Team Assignment**: Use existing Confederate and Union soldier creation
4. **Weapon Distribution**: Start with all characters using Brown Bess muskets

### **Battle Flow Questions** ✅
5. **Battle Duration**: No time limits, battles continue until manual victory declared
6. **Victory System**: CTRL-SHIFT-V triggers console interface for manual victory assignment
7. **Battle Pausing**: Already supported in existing game
8. **Spectator Mode**: Camera controls and unit following already exist

### **Character Management Questions** ✅
9. **Character Persistence**: Characters stored in shared `characters.json` file accessible to all scenarios
10. **Character Rosters**: Single unified roster in `characters.json` - no separate rosters needed
11. **Character Retirement**: Not implementing retirement system in DevCycle 8
12. **Character Replacement**: Pull replacement characters from existing `characters.json` pool

### **Character Storage Design** ✅
**Architecture**: Shared character database approach
- **Storage Location**: `characters.json` file contains all characters
- **Scenario Access**: All scenarios can access and use characters from this shared file
- **Character Creation**: New characters created and added to `characters.json`
- **Weapon Handling**: Characters stored without weapons - weapons assigned during scenario setup
- **Post-Battle Updates**: After scenario completion, update character stats back to `characters.json`

**Workflow**:
1. **Scenario Setup**: Pull existing characters from `characters.json`
2. **Equipment Phase**: Arm selected characters with scenario-appropriate weapons
3. **Battle Execution**: Run scenario with equipped characters
4. **Post-Battle**: Update character battle statistics back to `characters.json`

**Benefits**:
- Characters persist across all scenarios
- Flexible weapon assignment per scenario
- Centralized character management
- Battle experience accumulates over time

**Technical Implementation Details** ✅:
1. **Character Creation**: Batch creation through UI interface
2. **File Management**: No backup/versioning system needed initially
3. **Character Limits**: No artificial limits (JSON can handle large character counts efficiently)
4. **Faction Assignment**: Faction can be changed per scenario for flexibility

### **Character Creation and Deployment System** ✅
**Keyboard Controls**:
- **CTRL-C**: Create one or more new characters and save to `characters.json`
- **CTRL-A**: Add existing characters from `characters.json` to current scenario

**Workflow**:
1. **Character Creation Phase**: Use CTRL-C to create and persistently store characters
2. **Scenario Setup Phase**: Use CTRL-A to select and deploy existing characters
3. **Equipment Phase**: Arm deployed characters with scenario-appropriate weapons
4. **Battle Execution**: Run scenario with equipped characters

### **Manual Victory System** ✅
**Implementation**: Console-based victory declaration
- **Trigger**: CTRL-SHIFT-V hotkey during battle
- **Interface**: Console prompts for each faction outcome
- **Faction Results**: Victory, Defeat, or Participation credit per faction
- **Scenario End**: Battle terminates after victory outcomes assigned
- **Character Updates**: Victory results recorded in character battle statistics

### **Scenario Design Questions** ✅
13. **Battle Objectives**: Manual victory declaration via console interface
14. **Environmental Factors**: Not implementing terrain/weather yet
15. **Historical Accuracy**: Not a priority for initial implementation
16. **Scenario Variety**: Focus only on Civil War line battles for now

## **Success Metrics for DevCycle 8**

### **Minimum Viable Implementation**
- ✅ CTRL-C creates characters and saves to characters.json
- ✅ CTRL-A loads existing characters into current scenario
- ✅ Characters persist battle statistics across sessions
- ✅ Character data updates correctly after battles

### **Stretch Goals**
- ✅ Batch character creation (multiple characters at once)
- ✅ Character selection interface for deployment
- ✅ Faction assignment during character deployment
- ✅ Robust error handling for file operations

## **Connection to Future Cycles**

**DevCycle 9 Foundation**: The character management system built here enables:
- Formation setup and positioning tools
- Manual victory system (CTRL-SHIFT-V)
- Scenario definition and loading
- Battle of Testing Fields implementation
- Large-scale battle management

**Long-term Vision**: This simple start grows into:
- Complex multi-scenario campaigns
- Character-driven narrative emergence
- Historical battle recreation library
- RPG integration capabilities

## **Next Steps**

1. **Refine these open questions** through further discussion
2. **Prioritize features** for minimum viable vs stretch implementations
3. **Create technical implementation plan** when ready to move to DevCycle_2025_0008.md
4. **Begin with smallest viable test** (maybe even 5v5) to validate approach