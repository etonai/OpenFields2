package combat;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable value object representing a character's identity information.
 * Extracted from Character.java to improve code organization and reduce file size.
 */
public class CharacterIdentity {
    private final int id;
    private final String nickname;
    private final String firstName;
    private final String lastName;
    private final Date birthdate;
    private final String themeId;
    
    public CharacterIdentity(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId) {
        this.id = id;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate != null ? new Date(birthdate.getTime()) : null; // Defensive copy
        this.themeId = themeId;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public Date getBirthdate() {
        return birthdate != null ? new Date(birthdate.getTime()) : null; // Defensive copy
    }
    
    public String getThemeId() {
        return themeId;
    }
    
    /**
     * Returns the character's full display name
     */
    public String getDisplayName() {
        return nickname != null ? nickname : 
               (firstName != null || lastName != null) ? 
               (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "") : 
               "Character " + id;
    }
    
    /**
     * Creates a new CharacterIdentity with the same values except for the nickname
     */
    public CharacterIdentity withNickname(String newNickname) {
        return new CharacterIdentity(id, newNickname, firstName, lastName, birthdate, themeId);
    }
    
    /**
     * Creates a new CharacterIdentity with the same values except for the theme ID
     */
    public CharacterIdentity withThemeId(String newThemeId) {
        return new CharacterIdentity(id, nickname, firstName, lastName, birthdate, newThemeId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharacterIdentity that = (CharacterIdentity) o;
        return id == that.id &&
               Objects.equals(nickname, that.nickname) &&
               Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(birthdate, that.birthdate) &&
               Objects.equals(themeId, that.themeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, firstName, lastName, birthdate, themeId);
    }
    
    @Override
    public String toString() {
        return "CharacterIdentity{" +
               "id=" + id +
               ", nickname='" + nickname + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", birthdate=" + birthdate +
               ", themeId='" + themeId + '\'' +
               '}';
    }
}