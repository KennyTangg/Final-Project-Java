package com.contactsmanager.model;

// Model represent a contant with Name and Student ID

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
     * Returns the name of the contact
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
}