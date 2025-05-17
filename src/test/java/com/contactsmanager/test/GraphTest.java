package com.contactsmanager.test;

import com.contactsmanager.DataStructures.Graph;
import com.contactsmanager.model.Contact;

/**
 * Test class for the Graph (Adjacency List) implementation.
 */
public class GraphTest {
    public static void main(String[] args) {
        // Create a new Graph object
        Graph graph = new Graph();

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
        graph.printContact(); // Should not include Jane

        System.out.println("\n======== Test Deleting contact ========");
        graph.deleteContact("Jack");
        System.out.println("Searching for Jack after deletion: " + graph.searchContact("Jack"));  // Should return null

        System.out.println("\n======== Test DFS and BFS ========");
        System.out.println("DFS traversal from Justin:");
        graph.dfsTraversal(contact1);
        System.out.println("\nBFS traversal from Justin:");
        graph.bfsTraversal(contact1);
    }
}
