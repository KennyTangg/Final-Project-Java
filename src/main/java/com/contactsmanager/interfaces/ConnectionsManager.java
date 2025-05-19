package com.contactsmanager.interfaces;

import com.contactsmanager.model.Contact;

import java.util.List;

public interface ConnectionsManager {
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
