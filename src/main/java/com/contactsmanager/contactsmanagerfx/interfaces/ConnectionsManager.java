package com.contactsmanager.contactsmanagerfx.interfaces;

import com.contactsmanager.contactsmanagerfx.model.Contact;
import java.util.List;

/*
 * Interface for managing connections between contacts.
 * Also includes functionality for suggesting potential new connections, such as friends-of-friends.
 */
public interface ConnectionsManager {
    /**
     * Creates a connection between two contacts.
     * @param contact1 The name of the first contact
     * @param contact2 The name of the second contact
     */
    void addConnection(String contact1, String contact2);

    /**
     * Removes a connection between two contacts.
     * @param contact1 The name of the first contact
     * @param contact2 The name of the second contact
     */
    void removeConnection(String contact1, String contact2);

    /**
     * Suggests contacts that might be relevant to the given contact.
     * Will typically recommend friends-of-friends, excluding the contact themselves.
     *
     * @param contact The name of the contact to get suggestions for
     * @return List of suggested contacts
     */
    List<Contact> suggestContacts(String contact);
}
