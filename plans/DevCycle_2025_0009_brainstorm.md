# DevCycle 2025_0009 Brainstorm - Character Progression & Battle Systems
*Created: June 14, 2025*
*Based on features moved from DevCycle 8 brainstorm*

## **DevCycle 9 - Scenario System Foundation**
**Focus**: Large-scale battle scenarios and battle management systems

### **Core Systems to Build**
- **Formation setup and positioning tools** (both manual and predefined)
- **Manual victory system** (console-based faction outcome assignment)
- **Scenario definition and loading system**
- **Basic Battle of Testing Fields implementation**
- **Battle statistics and reporting**

### **Development Approach**
**Strategy**: Build on the character management foundation from DevCycle 8
- Begin with 10 vs 10 battles to identify obvious issues
- Scale up to 20 vs 20 if performance allows
- Eventually reach the goal of 40-character battles (20 vs 20)
- Focus on simple line formations rather than complex tactical positioning

### **Manual Victory System**
**Implementation**: Console-based victory declaration
- **Trigger**: CTRL-SHIFT-V hotkey during battle
- **Interface**: Console prompts for each faction outcome
- **Faction Results**: Victory, Defeat, or Participation credit per faction
- **Scenario End**: Battle terminates after victory outcomes assigned
- **Character Updates**: Victory results recorded in character battle statistics

### **Formation Setup and Positioning Tools**
- **Manual Positioning**: Click and drag character placement
- **Predefined Formations**: Line, Column, Skirmish Line templates
- **Team Assignment**: Use existing Confederate and Union soldier creation
- **Weapon Distribution**: Start with all characters using Brown Bess muskets

### **Battle Setup Requirements**
- **Battle Size Progression**: Start with 10v10, then scale up
- **Formation Templates**: Start with simple line formations
- **Battle Duration**: No time limits, battles continue until manual victory declared
- **Battle Pausing**: Already supported in existing game
- **Spectator Mode**: Camera controls and unit following already exist

## **Character Battle Tracking Requirements**

### **Battle Participation System**
Each character needs to track:
- **Battle Count**: Number of battles participated in
- **Win-Loss-Tie Record**: Performance across battles
- **Battle History**: Which battles they fought in (for narrative purposes)

### **Combat Performance Data** (Already Available from DevCycle 7)
Characters already track:
- **Shots Fired**: Total attacks attempted
- **Wounds Inflicted**: Damage dealt to opponents
- **Wounds Received**: Damage taken
- **Targets Incapacitated**: Enemies defeated

### **Victory Conditions Framework**
- **Manual victory declaration**: CTRL-SHIFT-V console interface
- **Faction outcome assignment**: Victory, Defeat, or Participation per faction
- **Battle duration tracking**: Record how long battles last
- **Survival tracking**: Which characters survive each battle
- **Post-battle character updates**: Update statistics in characters.json

## **Open Questions for DevCycle 9**

### **Battle Flow Questions**
1. **Battle Duration**: Should battles have time limits or run until user declares winner?
2. **Automatic vs Manual Victory**: Besides user declaration, should we detect when one side is clearly defeated?
3. **Battle Pausing**: Should large battles support pause/resume functionality for analysis?
4. **Spectator Mode**: How should the player observe 40-character battles? (Camera controls, unit following, etc.)

### **Character Management Questions**
5. **Character Persistence**: How do characters carry battle records between scenarios?
6. **Character Rosters**: Should we maintain separate rosters for different battle types?
7. **Character Retirement**: When should characters be considered too experienced/wounded for further battles?
8. **Character Replacement**: How to replace fallen characters for subsequent battles?

### **Experience and Progression Questions**
9. **Experience Calculation**: Simple survival vs performance-based?
   - Current insight: Besides battle wins vs losses, we know shots fired and wounds inflicted
10. **Skill Advancement**: Which skills should improve from battle experience?
11. **Progression Rate**: How quickly should characters advance after battles?
12. **Progression Caps**: Should there be limits to character advancement?

### **Battle Setup Questions** (Enhanced from DevCycle 8)
13. **Formation Templates**: What predefined formations should we support? (Line, Column, Skirmish Line, etc.)
14. **Team Assignment**: How should characters be assigned to Union vs Confederate sides?
15. **Weapon Distribution**: Should all characters get the same weapons or have period-appropriate variety?

### **Scenario Design Questions**
16. **Battle Objectives**: Beyond elimination, what other victory conditions make sense?
17. **Environmental Factors**: Should battles include terrain, weather, or other complications?
18. **Historical Accuracy**: How historically accurate should Civil War scenarios be?
19. **Scenario Variety**: What other battle types should we plan for after Civil War?

## **Success Metrics for DevCycle 9**

### **Minimum Viable Implementation**
- ✅ Track battle participation and results for all characters
- ✅ Implement user-declared victory conditions
- ✅ Calculate post-battle experience for survivors
- ✅ Enable character persistence between battles
- ✅ Generate basic battle statistics and reports

### **Stretch Goals**
- ✅ Support multiple formation types (line, column, etc.)
- ✅ Add battle statistics and post-battle reporting
- ✅ Implement automatic victory detection
- ✅ Create character roster management system
- ✅ Add character career tracking and advancement

## **Technical Considerations for DevCycle 9**

### **Save System Integration**
- **Character Rosters**: How to save/load persistent character groups
- **Battle History**: Storing battle records and outcomes
- **Experience Data**: Saving character progression between sessions

### **UI Enhancements**
- **Battle Management Interface**: Tools for managing large battles
- **Character Roster Display**: Viewing character careers and statistics
- **Post-Battle Reporting**: Summary of battle outcomes and character performance

### **Performance Considerations**
- **Battle Data Storage**: Efficient storage of battle history and statistics
- **Character Search/Filter**: Finding specific characters in large rosters
- **Battle Replay**: Potential for reviewing battle outcomes

## **Connection to Future Cycles**

**DevCycle 10 Foundation**: The character progression built here enables:
- Time-based wound healing systems
- Advanced character career management
- Character fame and reputation systems
- Multi-scenario campaign management

**Long-term Vision**: This progression system grows into:
- Complex multi-scenario campaigns with persistent characters
- Character-driven narrative emergence from battle experiences
- Historical battle recreation with veteran characters
- RPG integration with character import/export

## **Design Philosophy**

### **Character Development**
- **Merit-based advancement**: Characters improve based on performance and survival
- **Historical authenticity**: Progression feels appropriate to Civil War era
- **Narrative emergence**: Character stories develop naturally from battle experiences

### **Battle Management**
- **User control**: Player has authority over battle outcomes and management
- **Flexible systems**: Support various battle types and victory conditions
- **Data richness**: Detailed tracking enables future narrative features

## **Next Steps for DevCycle 9 Planning**

1. **Refine experience calculation formulas** based on available combat data
2. **Design character roster management interface** requirements
3. **Plan battle victory condition detection** algorithms
4. **Design post-battle reporting and statistics** displays
5. **Create technical implementation plan** when ready to move to DevCycle_2025_0009.md