package com.contactsmanager.DataStructures;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapCB implements ContactsManager {

    private Map<String, Contact> hash;

    public HashMapCB() {
        this.hash = new HashMap<>();
    }

    @Override
    public void addContact(Contact contact) {
        String key = contact.getName().toLowerCase();
        if (hash.containsKey(key)) {
            System.out.println("Contact already exists.");
            return;
        }
        hash.put(key, contact);
    }

    @Override
    public Contact searchContact(String name) {
        String key = name.toLowerCase();
        Contact result = hash.get(key);
        if (result == null) {
            System.out.println("Contact not found: " + name);
        }
        return result;
    }

    @Override
    public void deleteContact(String name) {
        String key = name.toLowerCase();
        if (hash.remove(key) != null) {
            System.out.println("Deleted contact: " + name);
        } else {
            System.out.println("Contact not found: " + name);
        }
    }

    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        String key = contact.getName().toLowerCase();
        if (!hash.containsKey(key)) {
            System.out.println("Contact not found.");
            return;
        }

        hash.remove(key); // Remove old key
        Contact updated = new Contact(newName, newStudentId);
        hash.put(updated.getName().toLowerCase(), updated);
    }

    @Override
    public List<Contact> listAllContacts() {
        return new ArrayList<>(hash.values());
    }
}