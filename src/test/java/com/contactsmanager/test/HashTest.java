package com.contactsmanager.test;

import com.contactsmanager.DataStructures.Hash;
import com.contactsmanager.model.Contact;

/**
 * Test class for the Hash implementation.
 */
public class HashTest {
    public static void main(String[] args) {
        // Create a new Hash object
        Hash hash = new Hash();

        // Create some contacts
        Contact contact1 = new Contact("John", 123);
        Contact contact2 = new Contact("Jane", 456);
        Contact contact3 = new Contact("Jack", 789);

        // Add contacts to the hash
        hash.addContact(contact1);
        hash.addContact(contact2);
        hash.addContact(contact3);

        System.out.println("\n======== Test Adding and Searching contacts ========");
        System.out.println("Searching for John: " + hash.searchContact("John"));  // Should return contact1
        System.out.println("Searching for non-existing contact: " + hash.searchContact("Max"));  // Should return null

        System.out.println("\n======== Test Updating a Contact ========");
        hash.updateContact(contact1, "Justin", 300);
        System.out.println("Updated contact: " + hash.searchContact("Justin"));

        System.out.println("\n======== Test Listing all contacts ========");
        System.out.println(hash.listAllContacts());

        System.out.println("\n======== Test Adding a Connection ========");
        hash.addConnection("Justin", "Jane");
        
        // Test suggesting contacts
        System.out.println("\n======== Test Suggesting Contacts ========");
        System.out.println("Suggested contacts for Justin: " + hash.suggestContacts("Justin"));
        
        // Add more connections to test suggestion
        Contact contact4 = new Contact("Alice", 111);
        hash.addContact(contact4);
        hash.addConnection("Jane", "Alice");
        System.out.println("Suggested contacts for Justin after adding Alice: " + hash.suggestContacts("Justin"));

        System.out.println("\n======== Test Removing a Connection ========");
        hash.removeConnection("Justin", "Jane");
        System.out.println("Suggested contacts after removing connection: " + hash.suggestContacts("Justin"));

        System.out.println("\n======== Test Deleting contact ========");
        hash.deleteContact("Jack");
        System.out.println("Searching for Jack after deletion: " + hash.searchContact("Jack"));  // Should return null
        System.out.println("Remaining contacts: " + hash.listAllContacts());
    }
}
