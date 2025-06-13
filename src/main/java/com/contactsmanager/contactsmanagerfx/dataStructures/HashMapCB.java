package com.contactsmanager.contactsmanagerfx.dataStructures;

import com.contactsmanager.contactsmanagerfx.interfaces.ConnectionsManager;
import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is a Contacts Book that is implemented using HashMap with adjacency list for connections.
 * Uses Map<String, Contact> for O(1) contact lookup and Map<String, List<String>> for O(1) connection management.
 * CB stands for Contacts Book.
 */
public class HashMapCB implements ContactsManager, ConnectionsManager {

    private final Map<String, Contact> contacts;
    private final Map<String, List<String>> connections;

    /**
     * Constructs a new, empty HashMap-based contact book with connection management.
     */
    public HashMapCB() {
        this.contacts = new HashMap<>();
        this.connections = new HashMap<>();
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
        if (contacts.containsKey(key)) {
            System.out.println("Contact with name '" + contact.getName() + "' already exists. Failed to put in contact.");
            return;
        }
        contacts.put(key, contact); // Add contact
        connections.put(key, new ArrayList<>()); // Initialize empty connection list
        System.out.println("Added contact. Name: '" + contact.getName() + "' | Student ID: " + contact.getStudentId() +".");
    }

    // UPDATE CONTACT
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        String oldKey = contact.getName().toLowerCase();
        String newKey = newName.toLowerCase();

        if (!contacts.containsKey(oldKey)) {
            System.out.println("Failed to get contact. It doesn't exist.");
            return;
        }

        // Transfer connections to new key if name changed
        List<String> contactConnections = connections.get(oldKey);
        contacts.remove(oldKey);
        connections.remove(oldKey);

        Contact updated = new Contact(newName, newStudentId);
        contacts.put(newKey, updated);
        connections.put(newKey, contactConnections != null ? contactConnections : new ArrayList<>());

        // Update references in other contacts' connection lists
        if (!oldKey.equals(newKey)) {
            for (List<String> connectionList : connections.values()) {
                for (int i = 0; i < connectionList.size(); i++) {
                    if (connectionList.get(i).equals(oldKey)) {
                        connectionList.set(i, newKey);
                    }
                }
            }
        }
    }

    // DELETE NODE
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContact(String name) {
        String key = name.toLowerCase();
        if (contacts.remove(key) != null) {
            // Remove all connections to this contact
            connections.remove(key);
            // Remove this contact from other contacts' connection lists
            for (List<String> connectionList : connections.values()) {
                connectionList.remove(key);
            }
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
        Contact result = contacts.get(key);
        if (result == null) {
            System.out.println("Contact not found: " + name);
        }
        return result;
    }

    /*========================================================================*/
    /*===== Connections Management ===========================================*/

    /*========================================================================*/
    /*===== Connections Management ===========================================*/

    @Override
    public void addConnection(String contact1, String contact2) {
        String key1 = contact1.toLowerCase();
        String key2 = contact2.toLowerCase();

        // Validate both contacts exist
        if (!contacts.containsKey(key1) || !contacts.containsKey(key2)) {
            System.out.println("One or both contacts not found.");
            return;
        }

        // Prevent self-connection
        if (key1.equals(key2)) {
            System.out.println("Cannot connect a contact to themselves.");
            return;
        }

        // Add bidirectional connection
        List<String> connections1 = connections.get(key1);
        List<String> connections2 = connections.get(key2);

        if (!connections1.contains(key2)) {
            connections1.add(key2);
            connections2.add(key1);
            System.out.println("Connection added between " + contact1 + " and " + contact2);
        } else {
            System.out.println("Connection between " + contact1 + " and " + contact2 + " already exists.");
        }
    }

    @Override
    public void removeConnection(String contact1, String contact2) {
        String key1 = contact1.toLowerCase();
        String key2 = contact2.toLowerCase();

        // Validate both contacts exist
        if (!contacts.containsKey(key1) || !contacts.containsKey(key2)) {
            System.out.println("One or both contacts not found.");
            return;
        }

        // Remove bidirectional connection
        List<String> connections1 = connections.get(key1);
        List<String> connections2 = connections.get(key2);

        if (connections1.remove(key2)) {
            connections2.remove(key1);
            System.out.println("Connection removed between " + contact1 + " and " + contact2);
        } else {
            System.out.println("No connection exists between " + contact1 + " and " + contact2);
        }
    }

    @Override
    public List<Contact> suggestContacts(String contact) {
        String key = contact.toLowerCase();
        List<Contact> suggestions = new ArrayList<>();

        if (!contacts.containsKey(key)) {
            System.out.println("Contact not found: " + contact);
            return suggestions;
        }

        List<String> directConnections = connections.get(key);
        if (directConnections.isEmpty()) {
            System.out.println("Unable to suggest contacts from not knowing anyone.");
            return suggestions;
        }

        // Use Set to avoid duplicates
        Set<String> suggested = new HashSet<>();

        // Find friends of friends
        for (String friendKey : directConnections) {
            List<String> friendsConnections = connections.get(friendKey);
            for (String friendOfFriendKey : friendsConnections) {
                // Don't suggest self or direct connections
                if (!friendOfFriendKey.equals(key) &&
                    !directConnections.contains(friendOfFriendKey) &&
                    !suggested.contains(friendOfFriendKey)) {
                    suggested.add(friendOfFriendKey);
                    suggestions.add(contacts.get(friendOfFriendKey));
                }
            }
        }

        if (suggestions.isEmpty()) {
            System.out.println(contact + "'s friends don't know anyone new.");
        }

        return suggestions;
    }

    /*========================================================================*/
    /*===== Printing and Getters Management ==================================*/

    // PRINT ALL CONTACTS
    /**
     * Print the contacts list on the terminal.
     */
    public void printContactsBook() {
        for (Map.Entry<String, Contact> entry : contacts.entrySet()) {
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
        return new ArrayList<>(contacts.values());
    }

    // RETURN THE HASHMAP
    /**
     * Getter for the HashMap.
     * @return the hashmap in Map<String, Contact>
     */
    public Map<String, Contact> getHashMap() {
        return contacts;
    }

    /**
     * Getter for the connections map.
     * @return the connections map in Map<String, List<String>>
     */
    public Map<String, List<String>> getConnections() {
        return connections;
    }
}
