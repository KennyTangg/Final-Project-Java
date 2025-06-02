package com.contactsmanager.contactsmanagerfx.model;

import java.util.Objects;

/**
 * Represents a contact with a name and student ID.
 */
public class Contact {
    private String name;
    private int studentId;

    /**
     * Constructs a new Contact with the specified name and student ID.
     *
     * @param name The name of the contact
     * @param studentId The student ID of the contact
     */
    public Contact(String name, int studentId) {
        this.name = name;
        this.studentId = studentId;
    }

    /**
     * Returns the name of the contact.
     * @return The contact's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the contact.
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the student ID of the contact.
     * @return The contact's student ID
     */
    public int getStudentId() {
        return this.studentId;
    }

    /**
     * Sets the student ID of the contact.
     * @param studentId The new student ID to set
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * Returns a string representation of the contact.
     * @return A string containing the name and student ID
     */
    @Override
    public String toString() {
        return "Contact { name=' " + name + " ', studentId= " + studentId + " }";
    }

    /**
     * Compares this contact with another object for equality.
     * Two contacts are considered equal if their names (trimmed, case-insensitive) and student IDs are the same.
     * @param o The object to compare with
     * @return true if the objects represent the same contact; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;

        // Compare student IDs
        if (studentId != contact.studentId) return false;

        // Compare names, handling null values and trimming whitespace
        if (name == null) {
            return contact.name == null;
        } else {
            return name.trim().equalsIgnoreCase(contact.name == null ? null : contact.name.trim());
        }
    }

    /**
     * Returns a hash code for the contact, based on the normalized name and student ID.
     * Ensures consistency with equals() for use in collections like HashMap or HashSet.
     * Without this override, even if two Contact objects have the same name and studentId, they'd go to different buckets in a HashMap.
     * @return A consistent hash code for the contact as int
     */
    @Override
    public int hashCode() {
        // Use the trimmed, lowercase name for hash code calculation to be consistent with equals
        String normalizedName = name == null ? null : name.trim().toLowerCase();
        return Objects.hash(normalizedName, studentId);
    }

}