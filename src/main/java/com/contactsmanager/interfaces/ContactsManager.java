package com.contactsmanager.interfaces;

import com.contactsmanager.model.Contact;
import java.util.List;

// Interface for managing contacts and their connections.

public interface ContactsManager {
    /**
     * Adds a new contact to the system.
     * @param contact The contact object to be added
     */
    void addContact(Contact contact);

    /**
     * Searches for a contact using name (can change to email, phone number or other identifier).
     * @param name The search key to find the contact
     * @return The found contact or null if not found
     */
    Contact searchContact(String name);

    /**
     * Deletes a contact from the system using name (can change to email, phone number or other identifier).
     * @param name The key of the contact to be deleted
     */
    void deleteContact(String name);

    /**
     * Updates an existing contact's information.
     * @param contact The contact object with updated information
     */
    void updateContact(Contact contact, String newName, int newStudentId);

    /**
     * Retrieves all contacts stored in the system.
     * @return List of all contacts
     */
    List<Contact> listAllContacts();

    /**
     * Creates a connection between two contacts.
     * @param contact1 The key of the first contact
     * @param contact2 The key of the second contact
     */
    void addConnection(String contact1, String contact2);

    /**
     * Removes a connection between two contacts.
     * @param contact1 The key of the first contact
     * @param contact2 The key of the second contact
     */
    void removeConnection(String contact1, String contact2);

    /**
     * Suggests contacts that might be relevant to the given contact.
     * @param contact The key of the contact to get suggestions for
     * @return List of suggested contacts
     */
    List<Contact> suggestContacts(String contact);
}
