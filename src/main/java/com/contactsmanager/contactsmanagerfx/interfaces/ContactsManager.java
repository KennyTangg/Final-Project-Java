package com.contactsmanager.contactsmanagerfx.interfaces;

import com.contactsmanager.contactsmanagerfx.model.Contact;
import java.util.List;

/*
 * Interface for managing contact objects within the data structure.
 * Primarily handles CRUD (Create Read Update Delete) operations.
 */
public interface ContactsManager {
    /**
     * Adds a new contact to the data structure.
     * In graph-based data structures, all connections will be initialized with no connections.
     *
     * @param contact The contact object to be added
     */
    void addContact(Contact contact);

    /**
     * Searches for a contact using name (case-sensitive match).
     * @param name The name used to find the contact
     * @return The matching contact object, or null if not found
     */
    Contact searchContact(String name);

    /**
     * Deletes a contact node from the data structure.
     * In graph-based implementations, also removes all connections to/ from the contact.
     * @param name The name of the contact to be deleted
     */
    void deleteContact(String name);

    /**
     * Updates the name and student ID of an existing contact.
     * Since Contact is used as a key, this method removes and reinserts the contact to ensure consistency in the map structure.
     * Existing connections are preserved.
     * @param contact The contact object that needs to be changed
     * @param newName The new name to replace the old contact's name
     * @param newStudentId New student ID as a replacement (can be the same as old one)
     */
    void updateContact(Contact contact, String newName, int newStudentId);

    /**
     * Retrieves all contacts stored in the system as a list.
     * @return List of all contacts
     */
    List<Contact> listAllContacts();
}
