class Character {
    String name;
    int dexterity;
    int health;
    double movementSpeed;
    Weapon weapon;

    public Character(String name, int dexterity, int health) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        // Weapon will be assigned in createUnits
        this.movementSpeed = 42.0;
    }
}

