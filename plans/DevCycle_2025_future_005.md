# DevCycle 2025-Future-005: Cover System - Height-Based Protection

## Core Concept: Height-Dependent Coverage
Cover effectiveness varies based on character stance and cover height relative to line of sight.

### Half-Height Cover Protection
- **Prone Characters**: 100% cover (completely protected behind half-height obstacles)
- **Kneeling Characters**: High protection percentage (75-90% based on cover height)
- EDNOTE: To simplify, 75% coverage for kneeling characters protected by half-height cover
- **Standing Characters**: Moderate protection (25-50% based on cover height)
- EDNOTE: To simplify, 50% coverage for standing characters protected by half-height cover
- **Crouching Characters**: Intermediate protection between kneeling and standing
- EDNOTE: No crouching is implemented in this game. Do not consider crouching.

### Full-Height Cover Protection
- **Complete Obstruction**: 100% protection when cover completely blocks line of sight
- **Partial Obstruction**: Variable protection based on visible target percentage

## Concealment System

### Core Concept: Visibility-Based Accuracy Reduction

Concealment does not physically stop bullets but makes targets harder to see and accurately engage. Uses similar geometric calculations as cover but applies accuracy penalties instead of damage protection.

### Full-Height Concealment
- **Dense Concealment** (large bush, thick vegetation): Significant accuracy penalty for shooters
- **Partial Concealment**: Variable accuracy reduction based on concealment density and target visibility
- **Line of Sight**: Target still detectable but harder to hit accurately

### Half-Height Concealment
- **Small Bushes**: Provide concealment for prone and kneeling characters
- **Tall Grass**: Affects visibility based on character stance relative to concealment height
- **Stance Interaction**: Similar height relationships as cover but applies accuracy penalties

### Concealment vs. Cover Mechanics
- **Cover**: Physical protection that stops/deflects bullets (damage reduction)
- EDNOTE: Cover is more about damage prevention than damage reduction. Damage reduction implies that a 30 point wound might be reduced to a 15 point wound, which isn't the case.
- **Concealment**: Visual obstruction that reduces accuracy (hit chance reduction)
- **Calculation Similarity**: Both use geometric line-of-sight and stance-based effectiveness
- **Combined Effects**: Target can benefit from both cover and concealment simultaneously

## Partial Cover Calculation System

### The 100% / 75% / 50% / 25% / 0% Protection Model
When full-height cover only partially obscures a target, calculate protection based on visible target area.

### Geometric Calculation Method
1. **Primary Line**: Draw line from weapon tip to target center point
2. **Perpendicular Construction**: From target center, draw perpendicular line to target circle perimeter
3. **Intercept Points**: Find where perpendicular line intersects target circle
4. **Visibility Sampling**: Use 5 sample points around target perimeter:
   - Center point
   - Two intercept points  
   - Two midpoints between center and intercepts
5. **Visibility Percentage**: Calculate how many sample points are visible from weapon position
6. **Protection Assignment**: Map visibility to protection tiers (0%, 25%, 50%, 75%, 100%)

### Implementation Considerations
- **Ray Casting**: Use line-of-sight calculations from weapon to each sample point
- **Cover Detection**: Determine which obstacles intersect each ray
- **Height Calculations**: Factor in attacker height, target stance, and cover height
- **Real-time Updates**: Recalculate protection as characters move or change stance

## Cover and Concealment System Integration Points
- **Movement System**: Stance changes (prone, kneeling, standing) affect both cover and concealment effectiveness
- **Combat System**: 
  - Cover protection percentages modify damage calculations
  - EDNOTE: More specifically, cover protection percentages prevent damage from taking place. It does not reduce damage that a character takes.
  - Concealment penalties modify hit chance calculations
  - Both effects can apply simultaneously
- **AI Behavior**: Characters seek appropriate cover/concealment based on stance and threat assessment
- **Visual Feedback**: Display cover effectiveness indicators for player awareness
- **Terrain System**: Distinguish between hard cover (walls, rocks) and concealment (vegetation, smoke)

## Technical Architecture Considerations

### Cover and Concealment System Implementation
- **Geometry Utilities**: New classes for line-of-sight and coverage calculations (shared between cover and concealment)
- **Cover Objects**: Extend terrain system to include height-based hard cover elements (walls, rocks, vehicles)
- **Concealment Objects**: Add vegetation and visual obstruction elements (bushes, grass, smoke)
- **Stance Management**: Integrate with existing movement system for character postures
- **Dual Effect Processing**: Calculate both cover protection and concealment penalties simultaneously
- **Performance Optimization**: Efficient algorithms for real-time coverage and concealment calculations

## Development Phases
1. **Phase 1**: Design and prototype cover and concealment system geometry
2. **Phase 2**: Integrate cover calculations with combat system (damage protection)
3. **Phase 3**: Integrate concealment calculations with combat system (accuracy penalties)
4. **Phase 4**: AI and player interface enhancements for cover and concealment utilization

## Research Questions
- What performance impact will real-time cover calculations have?
- EDNOTE - Do not worry about performance impact
- How should cover effectiveness integrate with existing accuracy modifiers?
- EDNOTE - Cover effectiveness is the last modifier to accuracy. 25% cover means that the total to-hit is reduced to 75% of the calculated number, and so on.
- Should different weapon types have different cover penetration capabilities?
- EDNOTE - We are not considering cover penetration at this time.
- How should stance transitions affect cover effectiveness in real-time?
- EDNOTE - Before implementing cover, we should convert the existing hit calculation to happen at the point the projectile reaches the target, instead of when the projectile is fired.
- What visual indicators will best communicate cover effectiveness to players?
- EDNOTE: No visual indicators.


EDNOTE: I have more thoughts on the half-height cover. We have to determine who qualifies to take half-height cover. At its most complicated, we need to calculate it based on shooter distance to cover, shooter gun height, target distance to cover. At its most complicated, at some distances the target can't take advantage of half-height cover, even if it is between shooter and target.
EDNOTE: A simpler method may just require the target to be within a certain distance of the cover to take advantage of it, even if this isn't exactly realistic.

## Half-Height Cover Qualification Analysis

### Geometric Approach (Most Realistic)

The key factors for realistic half-height cover calculation would be:

1. **Shooter's weapon height** (standing shooter ~5.5ft, kneeling ~3.5ft, prone ~1ft)
2. **Cover height** (typically ~3ft for half-height)
3. **Distance ratios**: Shooter-to-cover vs cover-to-target distances
4. **Target stance** (prone, kneeling, standing)

The geometric calculation would be:
- Draw a line from shooter's weapon to top of cover
- Extend this line to the target's position
- If line passes above target's center mass → target gets cover benefit
- If line passes through target → no cover benefit

### Simplified Distance-Based Approach (Recommended)

A simpler, more gameplay-friendly approach:

Target qualifies for half-height cover if:
1. Cover is between shooter and target (line intersection check)
2. Target is within X feet of cover (e.g., 7 feet / 1 square)
- 10 feet. We have no concept of 1 square in this game.
3. Target stance provides coverage:
   - Prone: Always qualifies if within distance
   - Kneeling: Qualifies if within distance
   - EDNOTE: Kneeling is partial benefit if within distance. More benefit than standing.
   - Standing: Only partial benefit (25-50%)

### Hybrid Approach (Balance of Realism and Simplicity)

1. Basic qualification: Cover between shooter and target
2. Distance threshold: Target within 10 feet of cover
3. Height check: 
   - If shooter elevated (>10ft height difference): No cover
   - If target prone/kneeling: Full benefit
   - If target standing: Partial benefit based on distance to cover

**Recommendation**: Start with the simplified distance-based approach. It's easier to implement, test, and understand for players, while still providing meaningful tactical choices. The system can be enhanced with more geometric complexity later if needed.
- EDNOTE: We will go with the recommendation of the simplified distance-based approach. 

## Current Hit Location System Analysis

### Hit Location Determination Components

The existing hit location system is located in **`CombatCalculator.java`** and will need modification for cover integration.

#### 1. Primary Method: `determineHitLocation(double randomRoll, double chanceToHit)`

**Location**: `/mnt/c/dev/TTCombat/OF2Prototype01/src/main/java/CombatCalculator.java:351-374`

The method uses a **tiered system based on shot quality**:

- **Excellent shots** (< 20% of hit chance): 15% head, 85% chest
- **Good shots** (< 70% of hit chance): 2% head, 49% chest, 49% abdomen  
- **Poor shots** (≥ 70% of hit chance): Random body part via `getRandomBodyPart()`

#### 2. Supporting Method: `getRandomBodyPart()`

**Location**: `CombatCalculator.java:376-386`

Distributes hits across all body parts:
- Arms: 12% each (left/right)
- Shoulders: 8% each (left/right) 
- Head: 10%
- Legs: 5% each (left/right)

#### 3. Body Parts Enum

**Location**: `/mnt/c/dev/TTCombat/OF2Prototype01/src/main/java/combat/BodyPart.java`

Defines 9 hit locations: HEAD, CHEST, ABDOMEN, LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG

#### 4. Integration Point

The method is called from `CombatCalculator.determineHit()` at line 109 as part of the complete hit resolution process that also determines wound severity and damage.

### Cover System Integration Requirements

For the cover system, we will need to create a **modified hit location determination** that:

1. **Filters available body parts** based on what's visible above/around cover
2. **Adjusts hit probabilities** for exposed vs. protected body parts
3. **Maintains shot quality tiers** but restricts them to visible body parts only

**Example for half-height cover with prone target:**
- EDNOTE: half-height cover with prone target will mean the target has 100% cover. But half-height cover with a kneeling target will mean head, shoulders, and part of chest is visible.
- Only HEAD visible → all hits go to head (if any hit at all)
- Standing target → HEAD, CHEST, ABDOMEN visible; arms/legs protected
- EDNOTE: A standing target does not have arms protected by half-height cover.

**Example for partial side cover:**
- Left side protected → no LEFT_ARM, LEFT_SHOULDER hits
- Redistribute those probabilities to visible right side and center mass
- EDNOTE: When behind full-height cover with partial side cover, one of the sides is protected and redistribute those probabilities to visible side. This is for arm, leg, and shoulder.