package com.contactsmanager.contactsmanagerfx.dataStructures;

import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a Contacts Book that is implemented using HashMap.
 * It does not implement connections between contacts.
 * CB stands for Contacts Book.
 */
public class HashMapCB implements ContactsManager {

    private final Map<String, Contact> hash;

    /**
     * Constructs a new, empty HashMap-based contact book.
     */
    public HashMapCB() {
        this.hash = new HashMap<>();
    }

    /*========================================================================*/
    /*===== Contacts/Node Management =========================================*/

    // ADD NODE
    /**
     * {@inheritDoc}
     */
    @Override
    public void addContact(Contact contact) {
        String key = contact.getName().toLowerCase(); // So it's just name in lowercase.
        if (hash.containsKey(key)) {
            System.out.println("Contact with name '" + contact.getName() + "' already exists. Failed to put in contact.");
            return;
        }
        hash.put(key, contact); // Add contact
        System.out.println("Added contact. Name: '" + contact.getName() + "' | Student ID: " + contact.getStudentId() +".");
    }

    // UPDATE CONTACT
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        String key = contact.getName().toLowerCase();
        if (!hash.containsKey(key)) {
            System.out.println("Failed to get contact. It doesn't exist.");
            return;
        }

        hash.remove(key); // Remove old key
        Contact updated = new Contact(newName, newStudentId);
        hash.put(updated.getName().toLowerCase(), updated);
    }

    // DELETE NODE
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContact(String name) {
        String key = name.toLowerCase();
        if (hash.remove(key) != null) {
            System.out.println("Deleted contact: " + name);
        } else {
            System.out.println("Contact not found: " + name);
        }
    }

    // SEARCH NODE
    /**
     * {@inheritDoc}
     */
    @Override
    public Contact searchContact(String name) {
        String key = name.toLowerCase();
        Contact result = hash.get(key);
        if (result == null) {
            System.out.println("Contact not found: " + name);
        }
        return result;
    }

    /*========================================================================*/
    /*===== Connections Management ===========================================*/

    // PRINT ALL CONTACTS
    /**
     * Print the contacts list on the terminal.
     */
    public void printContactsBook() {
        for (Map.Entry<String, Contact> entry : hash.entrySet()) {
            Contact contact = entry.getValue();
            System.out.println("Name: " + contact.getName() + " | Student ID: " + contact.getStudentId());
        }
    }

    // RETURN ALL CONTACTS
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> listAllContacts() {
        return new ArrayList<>(hash.values());
    }

    // RETURN THE HASHMAP
    /**
     * Getter for the HashMap.
     * @return the hashmap in Map<String, Contact>
     */
    public Map<String, Contact> getHashMap() {
        return hash;
    }
}
