class Character {
    String name;
    int dexterity;
    int health;
    double movementSpeed;
    Weapon weapon;

    public Character(String name, int dexterity, int health, Weapon weapon) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.weapon = weapon;
        this.movementSpeed = 42.0;
    }

    public String getName() {
        return name;
    }

}

