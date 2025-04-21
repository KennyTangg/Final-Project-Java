package com.contactsmanager.manager;

import com.contactsmanager.model.Contact;
import java.util.List;

public interface ContactsManager {
    void addContact(Contact contact);
    Contact searchContact(String key);
    void deleteContact(String key);
    void updateContact(Contact contact);
    List<Contact> listAllContacts();

    void addConnection(String contactKey1, String contactKey2);
    void removeConnection(String contactKey1, String contactKey2);
    List<Contact> suggestContacts(String contactKey);
}
