# DevCycle 2025_0012 Brainstorm - Scenario Systems & Character Progression
*Created: June 16, 2025*
*Moved from DevCycle 9 and 11 brainstorm - focusing Dev Cycle 9 on Melee Combat only*

## **DevCycle 10 - Scenario System Foundation**
**Focus**: Large-scale battle scenarios and battle management systems

### **Core Systems to Build**
- **Formation setup and positioning tools** (both manual and predefined)
- **Scenario definition and loading system**
- **Basic Battle of Testing Fields implementation**
- **Battle statistics and reporting**

### **Development Approach**
**Strategy**: Build on the character management foundation from DevCycle 8
- Begin with 10 vs 10 battles to identify obvious issues
- Scale up to 20 vs 20 if performance allows
- Eventually reach the goal of 40-character battles (20 vs 20)
- Focus on simple line formations rather than complex tactical positioning

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
- **Battle duration tracking**: Record how long battles last
- **Survival tracking**: Which characters survive each battle
- **Post-battle character updates**: Update statistics in characters.json

## **Open Questions for DevCycle 10**

### **Battle Flow Questions**
1. **Battle Duration**: Should battles have time limits or run until automatic victory detection?
2. **Automatic Victory Detection**: How should we detect when one side is clearly defeated?
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

## **Success Metrics for DevCycle 10**

### **Minimum Viable Implementation**
- ✅ Track battle participation and results for all characters
- ✅ Calculate post-battle experience for survivors
- ✅ Enable character persistence between battles
- ✅ Generate basic battle statistics and reports

### **Stretch Goals**
- ✅ Support multiple formation types (line, column, etc.)
- ✅ Add battle statistics and post-battle reporting
- ✅ Implement automatic victory detection
- ✅ Create character roster management system
- ✅ Add character career tracking and advancement

## **Technical Considerations for DevCycle 10**

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

**DevCycle 11 Foundation**: The character progression built here enables:
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

## **Enhanced Wound Tracking System** *(Moved from DevCycle 9)*

### **Detailed Wound Information**
**Core Concept**: Expand wound tracking to include comprehensive combat context
- **Body Part Tracking**: Specific location of each wound (head, chest, abdomen, arms, legs, etc.)
- **Severity Classification**: Scratch, Light, Serious, Critical wound categories
- **Damage Values**: Actual hit points lost per wound for medical/healing systems
- **Weapon Attribution**: Which weapon and ammunition type caused each wound
- **Combat Context**: When and how each wound was received

### **Wound Data Structure**
**Enhanced wound records should capture**:
```
Body Part: left_shoulder
Severity: light
Damage: 1 
Weapon: wpn_uzi
Ammunition: 9mm round
Battle Context: [Optional battle/scenario info]
```

**Example wound tracking data**:
- `left_shoulder: light, 1 damage (from 9mm round, weapon: wpn_uzi)`
- `abdomen: critical, 1 damage (from 9mm round, weapon: wpn_uzi)`

### **System Applications**
**Medical and Healing Systems**:
- **Wound-specific healing rates**: Different body parts and severities heal differently
- **Medical treatment targeting**: Treatments can focus on specific wound types
- **Character capability impact**: Wounds affect character performance based on location
- **Battle narrative**: Detailed wound history creates character stories

**Tactical Intelligence**:
- **Weapon effectiveness analysis**: Track which weapons cause what damage patterns
- **Ammunition performance**: Compare effectiveness of different round types
- **Body armor planning**: Identify most vulnerable body areas for protection
- **Medical preparedness**: Plan medical resources based on typical wound patterns

### **Integration with Existing Systems**
**Character Statistics**:
- **Wound count tracking**: Current system tracks total wounds received
- **Performance impact**: Wounds affect character combat effectiveness
- **Survival tracking**: Critical wounds impact character incapacitation
- **Career records**: Wound history becomes part of character biography

**Combat Resolution**:
- **Hit location determination**: Combat system determines where shots land
- **Damage calculation**: Existing damage system provides wound severity
- **Weapon identification**: Combat resolver knows which weapon caused damage
- **Battle context**: Scenario and engagement data available for wound records

### **Implementation Considerations**
**Data Storage**:
- **Enhanced Wound class**: Expand to include weapon, ammunition, and context data
- **Character persistence**: Faction files store detailed wound histories
- **Battle logging**: Record wound events during combat for post-battle analysis
- **Medical system foundation**: Wound data enables future healing and treatment systems

**Performance Impact**:
- **Memory usage**: Detailed wound tracking increases character data size
- **Save file size**: Enhanced wound records require more storage space
- **Processing efficiency**: Wound lookup and analysis operations need optimization
- **User interface**: Wound display systems need to present rich data clearly

### **Design Questions for Enhanced Wound Tracking**
1. **Wound Persistence**: Should wounds heal over time or remain permanent battle scars?
2. **Medical Intervention**: Should characters be able to receive treatment to reduce wound impact?
3. **Wound Visualization**: How should detailed wound information be displayed to players?
4. **Battle Analysis**: Should wound patterns be used for post-battle tactical analysis?
5. **Historical Accuracy**: Should wound tracking reflect period-appropriate medical understanding?
6. **Narrative Integration**: How can wound histories contribute to character storytelling?

### **Connection to Character Progression**
**Experience and Skills**:
- **Survival bonuses**: Characters gain experience for surviving serious wounds
- **Medical skills**: Wounded characters might develop first aid or medical knowledge
- **Pain tolerance**: Repeated wounding could improve character resilience
- **Battle wisdom**: Wound patterns teach characters tactical awareness

## **Next Steps for DevCycle 10 Planning**

1. **Refine experience calculation formulas** based on available combat data
2. **Design character roster management interface** requirements
3. **Plan battle victory condition detection** algorithms
4. **Design post-battle reporting and statistics** displays
5. **Design enhanced wound tracking implementation** for medical systems foundation
6. **Create technical implementation plan** when ready to move to DevCycle_2025_0010.md