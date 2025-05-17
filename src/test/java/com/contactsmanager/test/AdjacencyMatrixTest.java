package com.contactsmanager.test;

import com.contactsmanager.DataStructures.AdjacencyMatrixGraph;
import com.contactsmanager.model.Contact;

/**
 * Test class for the AdjacencyMatrixGraph implementation.
 */
public class AdjacencyMatrixTest {
    public static void main(String[] args) {
        // Create a new AdjacencyMatrixGraph object with capacity of 10
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(10);

        // Create some contacts
        Contact contact1 = new Contact("John", 123);
        Contact contact2 = new Contact("Jane", 456);
        Contact contact3 = new Contact("Jack", 789);

        // Add contacts to the graph
        graph.addContact(contact1);
        graph.addContact(contact2);
        graph.addContact(contact3);

        System.out.println("\n======== Test Adding and Searching contacts ========");
        System.out.println("Searching for John: " + graph.searchContact("John"));  // Should return contact1
        System.out.println("Searching for non-existing contact: " + graph.searchContact("Max"));  // Should return null

        System.out.println("\n======== Test Updating a Contact ========");
        graph.updateContact(contact1, "Justin", 300);
        System.out.println(contact1);

        System.out.println("\n======== Test Printing the contact list ========");
        graph.printContact();

        System.out.println("\n======== Test Adding a Connection ========");
        graph.addConnection("Justin", "Jane");
        graph.printContact();

        System.out.println("\n======== Test Removing a Connection ========");
        graph.removeConnection("Justin", "Jane");
        graph.printContact();

        System.out.println("\n======== Test Deleting contact ========");
        graph.deleteContact("Jack");
        System.out.println("Searching for Jack after deletion: " + graph.searchContact("Jack"));  // Should return null

        // Test capacity limit
        System.out.println("\n======== Test Capacity Limit ========");
        for (int i = 0; i < 10; i++) {
            Contact contact = new Contact("Test" + i, 1000 + i);
            graph.addContact(contact);
        }
        graph.printContact();
    }
}
