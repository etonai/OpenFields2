# Test Scenarios Analysis - Post DevCycle 33

This document captures all development notes and decisions for the Test Scenario System that will be implemented in DevCycle 2025-Future-004.

## Core Concept: Deterministic Test Scenarios for Combat Validation

A comprehensive test scenario system that provides predictable, repeatable combat situations for both automated and manual testing.

### Test Scenario Categories

#### Combat Accuracy Tests
- **Perfect Shooter Scenarios**: Characters with guaranteed hits for testing damage, wound locations, and combat effects
- **Guaranteed Miss Scenarios**: Characters that never hit for testing miss mechanics and continued combat
- **Average Performance Scenarios**: Baseline characters like Chris for standard gameplay validation

#### Weapon System Tests
- **Ranged Weapon Scenarios**: Pistols, rifles, burst weapons with known accuracy outcomes
- **Melee Combat Scenarios**: Close combat with predictable hit/miss patterns
- **Reload and Ammunition Tests**: Multi-round combat scenarios with reloading mechanics

#### Character State Tests
- **Movement Impact Tests**: Combat while moving at different speeds
- **Position State Tests**: Prone, standing combat effectiveness
- **Wound Effect Tests**: Combat performance degradation from wounds

## Test Character Specifications

### Deterministic Test Characters

#### Perfect Shooter (Test Character Alpha)
- **Dexterity**: 100 (maximum accuracy modifier: +20)
- **All Combat Skills**: Maximum level
- **EDNOTE**: Maximum skill is not 100. It might be 10. It is not yet determined, but skills for the vast majority of situations should range from 0 - 7.
- **Weapon Accuracy**: High-accuracy weapons only
- **Purpose**: Guarantee hits for testing damage systems, wound locations, combat effects

#### Guaranteed Miss (Test Character Beta)  
- **Dexterity**: 1 (minimum accuracy modifier: -20)
- **All Combat Skills**: Minimum level or no skills
- **EDNOTE**: The minimum skill level is not 1, or even 0. The worst situation is if a character doesn't even have the skill.
- **Weapon Accuracy**: Low-accuracy weapons only
- **Additional Penalties**: Movement, stress, bravery failures
- **Purpose**: Guarantee misses for testing miss mechanics, ammo consumption, continued combat

#### Baseline Average (Chris - Existing)
- **All Stats**: 50 (zero modifiers)
- **Skills**: Baseline levels
- **Purpose**: Standard gameplay scenarios, regression testing

### Specialized Test Characters

#### Glass Cannon (Test Character Gamma)
- **Health**: 1 (dies from any wound)
- **Combat Stats**: High accuracy
- **Purpose**: Test incapacitation, death mechanics, combat resolution

#### Tank (Test Character Delta)
- **Health**: 200 (intended to survive multiple wounds)
- **EDNOTE**: This will not work because critical wounds always incapacitate a character, no matter their health
- **Combat Stats**: Average
- **Purpose**: Test wound accumulation, health degradation over time
- **REVISED PURPOSE**: Test multiple non-critical wounds since critical wounds cause immediate incapacitation

## Test Scenario Save System

### Save Key Mapping
- **Standard Saves**: Numeric keys 1-9 (existing system)
- **Test Scenarios**: Alphabetic keys A-Z (26 test scenarios)
- **Display Logic**: Test scenarios visible only when debug mode enabled

### Debug Mode Integration
```
When debug mode enabled and user presses CTRL-L or CTRL-S:
- Display both standard saves (1-9) and test scenarios (A-Z)
- Format: "A. Perfect Accuracy vs Glass Cannon (test_theme, tick 0)"
- Load immediately when alphabetic key pressed
```

### Test Scenario Library

#### Core Combat Tests
- **Scenario A**: Perfect Shooter vs Glass Cannon (single guaranteed hit/kill)
- **Scenario B**: Guaranteed Miss vs Tank (extended miss sequence)
- **Scenario C**: Average Combat (Chris vs equivalent opponent)
- **Scenario D**: Burst Fire Test (full auto weapon scenarios)
- **Scenario E**: Reload Under Fire (ammunition management)

#### Movement and Position Tests
- **Scenario F**: Moving Target Accuracy (various movement speeds)
- **Scenario G**: Prone vs Standing Combat (position advantages)
- **Scenario H**: Multi-Character Melee (close combat scenarios)

#### Weapon System Tests
- **Scenario I**: Ranged Weapon Comparison (pistol vs rifle vs SMG)
- **Scenario J**: Melee Weapon Effectiveness (various melee weapons)
- **Scenario K**: Weapon State Transitions (holstered to firing sequences)

#### Advanced Combat Tests
- **Scenario L**: Wound Accumulation (multiple non-fatal wounds)
- **Scenario M**: Bravery and Stress Effects (morale failure scenarios)
- **Scenario N**: Multi-Target Engagement (target switching scenarios)

## Key Design Decisions and Constraints

### Skill System Constraints
- **Maximum Skill Level**: Unknown, possibly 10, typically 0-7 range
- **Minimum Skill Level**: No skill (worse than level 0)
- **Implication**: Test characters need extreme skill configurations within actual system limits

### Combat System Constraints
- **Critical Wounds**: Always cause incapacitation regardless of health
- **Implication**: "Tank" character concept needs revision - focus on non-critical wound accumulation
- **Health System**: Health reduction still occurs even with critical wound incapacitation

### Save System Integration
- **Alphabetic Keys**: A-Z for test scenarios (26 total possible)
- **Debug Mode Only**: Test scenarios not visible in normal play
- **Existing System**: Must work alongside current 1-9 numeric save system

## Implementation Requirements

### Test Character Generation
- **Extreme Stat Distributions**: Characters with maximum/minimum possible values
- **Skill Configuration**: Account for actual skill system limits
- **Weapon Selection**: Choose weapons that maximize/minimize hit probability
- **Additional Modifiers**: Apply movement, stress, bravery effects for guaranteed miss characters

### Scenario Data Structure
```json
{
  "scenarioId": "A",
  "name": "Perfect Accuracy vs Glass Cannon",
  "description": "Single guaranteed hit resulting in immediate incapacitation",
  "characters": [
    {
      "id": "test_alpha",
      "position": [100, 100],
      "faction": 1,
      "weapon": "wpn_colt_peacemaker"
    }
  ],
  "expectedOutcome": {
    "hitCount": 1,
    "missCount": 0,
    "incapacitations": ["test_beta"],
    "maxTicks": 100
  }
}
```

### Automated Test Framework
- **TestScenarioRunner**: Execute scenarios and capture results
- **Output Validation**: Compare actual vs. expected outcomes
- **Statistical Analysis**: Handle scenarios with some variance
- **Report Generation**: Document test results and failures

## Quality Assurance Applications

### Manual Testing Benefits
- **Reproducible Issues**: Load exact scenario causing bugs
- **Edge Case Testing**: Pre-configured extreme situations
- **Development Workflow**: Quick scenario loading for feature testing

### Automated Testing Benefits
- **Regression Testing**: Validate all scenarios after changes
- **CI/CD Integration**: Automated test execution in build pipeline
- **Performance Testing**: Long-running scenarios for stress testing

## Development Strategy

### Phase 1: Foundation
1. Research actual skill system limits and constraints
2. Design test characters within system boundaries
3. Create basic scenario loading infrastructure

### Phase 2: Core Scenarios
1. Implement deterministic combat scenarios (perfect hit/miss)
2. Build save system extensions for alphabetic keys
3. Create initial test scenario library

### Phase 3: Advanced Testing
1. Add statistical validation for variable scenarios
2. Implement automated test execution framework
3. Integrate with development workflow

### Phase 4: Expansion
1. Create comprehensive scenario library covering all combat systems
2. Add performance and stress testing scenarios
3. Document usage patterns for development team

## Critical Success Factors

### Deterministic Behavior
- Test characters must provide guaranteed outcomes
- Scenarios must be repeatable across multiple runs
- Random elements must be controlled or eliminated

### System Integration
- Must work with existing save/load system
- Debug mode integration required
- No modifications to core combat system needed

### Development Efficiency
- Quick scenario loading for rapid iteration
- Clear expected outcomes for easy validation
- Minimal maintenance overhead for scenario updates

This analysis provides the foundation for implementing a robust test scenario system that will greatly improve both automated testing and manual QA processes for the OpenFields2 combat system.