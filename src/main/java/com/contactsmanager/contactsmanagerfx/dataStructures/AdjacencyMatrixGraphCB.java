package com.contactsmanager.contactsmanagerfx.dataStructures;

import com.contactsmanager.contactsmanagerfx.interfaces.ConnectionsManager;
import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is a Contacts Book that is implemented using Graph (made from Adjacency Matrix).
 * Unlike adjacency list, this one has a fixed limit/ capacity.
 * Uses 1s and 0s as bytes to tell the connection. Row = from, Column = to.
 * CB stands for Contacts Book.
 *
 * Extends AbstractGraphCB to inherit common graph functionality like
 * directionality management, connection validation, and standardized messaging.
 */
public class AdjacencyMatrixGraphCB extends AbstractGraphCB implements ContactsManager, ConnectionsManager {
    int size;
    int maxSize;
    byte[][] matrix; // Where the connections are stored
    Contact[] contactsBook; // Where the contact information are stored

    /**
     * Constructs an undirected contacts graph of maxSize size.
     *
     * @param maxSize Maximum size of contacts (people) that can be held.
     */
    public AdjacencyMatrixGraphCB(int maxSize) {
        this(maxSize, false);
    }

    /**
     * Constructs contacts graph of maxSize size with directionality defined.
     *
     * @param maxSize  Maximum size of contacts (people) that can be held.
     * @param directed Directed graph or not.
     */
    public AdjacencyMatrixGraphCB(int maxSize, boolean directed) {
        super(directed); // Call parent constructor with directionality
        this.size = 0;
        this.maxSize = maxSize;
        matrix = new byte[maxSize][maxSize];
        contactsBook = new Contact[maxSize];
    }

    /*========================================================================*/
    /*===== Abstract Methods Implementation ==================================*/

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean connectionExists(String contact1, String contact2) {
        int index1 = searchIndexOfContact(contact1);
        int index2 = searchIndexOfContact(contact2);

        if (index1 == -1 || index2 == -1) {
            return false;
        }

        return matrix[index1][index2] == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Contact[] getContactPair(String contact1Name, String contact2Name) {
        int index1 = searchIndexOfContact(contact1Name);
        int index2 = searchIndexOfContact(contact2Name);

        if (index1 == -1 || index2 == -1) {
            return null;
        }

        return new Contact[]{contactsBook[index1], contactsBook[index2]};
    }

    /*========================================================================*/
    /*===== Contacts/Node Management =========================================*/

    // ADD NODE
    /**
     * {@inheritDoc}
     *
     * @param contact The contact object to be added
     */
    @Override
    public void addContact(Contact contact) {
        if (size == maxSize) { // If full
            System.out.println("Matrix is full. Cannot add more nodes.");
            return;
        }
        int free = searchIndexOfFree();
        if (searchIndexOfContact(contact.getName()) == -1) { // Only add if the name doesn't have the person with the same name yet.
            contactsBook[free] = contact;
            size++;
            System.out.println("Added contact. Name: '" + contact.getName() + "' | Student ID: " + contact.getStudentId() + ".");
        } else {
            System.out.println("Contact with name '" + contact.getName() + "' already exists. Failed to put in contact.");
        }
    }

    // UPDATE CONTACT
    /**
     * {@inheritDoc}
     *
     * @param contact The contact object that needs to be changed
     * @param newName The new name to replace the old contact's name
     * @param newStudentId New student ID as a replacement (can be the same as old one)
     */
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        String name = contact.getName();
        int target = searchIndexOfContact(name);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: " + name);
            return;
        }

        Contact newContact = new Contact(newName, newStudentId); // Make new contact to replace old one
        contactsBook[target] = newContact;
    }

    // DELETE NODE
    /**
     * {@inheritDoc}
     *
     * @param name The name of the contact to be deleted
     */
    @Override
    public void deleteContact(String name) {
        int target = searchIndexOfContact(name);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: '" + name + "'. Deletion unsuccessful.");
            return;
        }

        for (int j = 0; j < maxSize; j++) { // Delete target's known connections
            matrix[target][j] = 0;
        }
        for (int i = 0; i < maxSize; i++) { // Delete connections of people who had connections to the target.
            matrix[i][target] = 0;
        }

        contactsBook[target] = null; // Delete contact info
        size--;
        System.out.println("Deleted contact: " + name);
    }

    // SEARCH NODE
    /**
     * {@inheritDoc}
     *
     * @param name The name used to find the contact
     * @return The matching contact object, or null if not found
     */
    @Override
    public Contact searchContact(String name) {
        for (Contact contact : contactsBook) {
            if (contact != null && contact.getName().equals(name)) {
                return contact;
            }
        }
        System.out.println("Contact not found: " + name);
        return null;
    }

    // HELPER FUNCTION: INTERNALLY SEARCH CONTACT BY INDEX
    private int searchIndexOfContact(String name) {
        int i = 0;
        for (Contact contact : contactsBook) {
            if (contact != null && contact.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    // HELPER FUNCTION: INTERNALLY SEARCH BLANK SPACE IN CONTACT LIST
    private int searchIndexOfFree() {
        if (size == maxSize) {
            System.out.println("No space left in contacts book.");
            return -1;
        }
        int i = 0;
        for (Contact contact : contactsBook) {
            if (contact == null) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /*========================================================================*/
    /*===== Connections Management ===========================================*/

    // ADD CONNECTION
    /**
     * {@inheritDoc}
     *
     * @implSpec Adds a one-way or two-way connection depending on directionality.
     * @param contact1 The name of the first contact
     * @param contact2 The name of the second contact
     */
    @Override
    public void addConnection(String contact1, String contact2) {
        // Find index of both strings for the matrix
        int fromIndex = searchIndexOfContact(contact1); // y = from
        int toIndex = searchIndexOfContact(contact2); // x = to

        // Check if both contacts exist
        if (fromIndex == -1 || toIndex == -1) {
            System.out.println("One or both contacts not found. Cannot add connection.");
            return;
        }

        matrix[fromIndex][toIndex] = 1;
        if (!directed) { // Undirected graph
            matrix[toIndex][fromIndex] = 1;
        }
    }

    // DELETE CONNECTION
    /**
     * {@inheritDoc}
     *
     * @implSpec Deletes a one-way or two-way connection depending on directionality.
     * @param contact1 The name of the first contact
     * @param contact2 The name of the second contact
     */
    @Override
    public void removeConnection(String contact1, String contact2) {
        // Find index of both strings for the matrix
        int fromIndex = searchIndexOfContact(contact1); // y = from
        int toIndex = searchIndexOfContact(contact2); // x = to

        // Check if both contacts exist
        if (fromIndex == -1 || toIndex == -1) {
            System.out.println("One or both contacts not found. Cannot remove connection.");
            return;
        }

        matrix[fromIndex][toIndex] = 0;
        if (!directed) { // Undirected graph
            matrix[toIndex][fromIndex] = 0;
        }
    }

    // SUGGEST CONTACTS
    /**
     * {@inheritDoc}
     *
     * @implSpec Will not suggest the user itself. Will recommend the person's friends' friends.
     * @param contact The name of the contact to get suggestions for
     * @return List of suggested contacts
     */
    @Override
    public List<Contact> suggestContacts(String contact) {
        LinkedList<Integer> directConnectionsIndex = new LinkedList<>();
        HashSet<Integer> recommendedContactsIndex = new HashSet<>();
        LinkedList<Contact> recommendedContacts = new LinkedList<>();
        int target = searchIndexOfContact(contact);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: " + contact);
            return recommendedContacts; // Return empty list
        }

        for (int i = 0; i < maxSize; i++) { // Find person's friends (by index)
            if (matrix[target][i] == 1) {
                directConnectionsIndex.add(i);
            }
        }
        if (directConnectionsIndex.isEmpty()) {
            System.out.println("Unable to suggest contacts from not knowing anyone.");
            return recommendedContacts; // Return empty list instead of null
        }
        for (int directConnection : directConnectionsIndex) { // Find friend's friends (by index)
            for (int i = 0; i < maxSize; i++) {
                if ((matrix[directConnection][i] == 1) && (i != target)) {
                    recommendedContactsIndex.add(i);
                }
            }
        }
        for (int suggested : recommendedContactsIndex) { // Change indexes to contact
            Contact suggestedContact = contactsBook[suggested];
            if (suggestedContact != null) {
                recommendedContacts.add(suggestedContact);
            }
        }
        if (recommendedContacts.isEmpty()) {
            System.out.println(contact + "'s friends doesn't know anyone.");
        }

        return recommendedContacts;
    }

    /*========================================================================*/
    /*===== Printing and Getters Management ==================================*/

    // PRINT ALL CONTACTS
    /**
     * Print the contacts list on the terminal.
     */
    public void printContact() {
        for (int i = 0; i < maxSize; i++) { // Print person
            System.out.print(i + ") " + String.format("%-50s", contactsBook[i]) + " : ");

            for (int j = 0; j < maxSize; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println();
        }
    }

    // LIST ALL CONTACTS
    /**
     * {@inheritDoc}
     *
     * @return List of all contacts
     */
    @Override
    public List<Contact> listAllContacts() {
        List<Contact> result = new ArrayList<>();
        for (Contact contact : contactsBook) {
            if (contact != null) {
                result.add(contact);
            }
        }
        return result;
    }

    // RETURN THE ADJACENCY MATRIX
    /**
     * Getter for the Adjacency Matrix
     * @return the adjacency matrix in byte[][]
     */
    public byte[][] getMatrix() {
        return matrix;
    }

    // RETURN THE CONTACTS
    /**
     * Getter for the Contact information array
     * @return the contacts information in Contact[]
     */
    public Contact[] getContactsBook() {
        return contactsBook;
    }

    // RETURN THE MAX SIZE
    /**
     * Getter for the Max size
     * @return the max size of the matrix in integer
     */
    public int getMaxSize() {
        return maxSize;
    }

    /*========================================================================*/
    /*===== Traversal Management =============================================*/

    // TRAVERSAL: BREADTH FIRST SEARCH
    /**
     * Breadth first search traversal that abides to one-way connections.
     * @param contact The name of the contact to start from
     */
    public void bfsTraversal(String startName) {
        int startIndex = searchIndexOfContact(startName);
        if (startIndex == -1) {
            System.out.println("Start contact does not exist in this graph.");
            return;
        }

        boolean[] visited = new boolean[maxSize];
        LinkedList<Integer> queue = new LinkedList<>();

        visited[startIndex] = true;
        queue.add(startIndex);

        while (!queue.isEmpty()) {
            int currentIndex = queue.poll();
            System.out.println("Visited:[ Name: " + contactsBook[currentIndex].getName() + " | Student ID: " + contactsBook[currentIndex].getStudentId() + " ]");

            for (int i = 0; i < maxSize; i++) {
                if (matrix[currentIndex][i] == 1 && !visited[i]) {
                    visited[i] = true;
                    queue.add(i);
                }
            }
        }
    }

    // TRAVERSAL: DEPTH FIRST SEARCH
    /**
     * Depth first search traversal that abides to one-way connections.
     * @param contact The name of the contact to start from
     */
    public void dfsTraversal(String startName) {
        int startIndex = searchIndexOfContact(startName);
        if (startIndex == -1) {
            System.out.println("Start contact does not exist in this graph.");
            return;
        }

        boolean[] visited = new boolean[maxSize];
        dfsVisit(startIndex, visited);
    }

    // HELPER FUNCTION: RECURSIVE FUNCTION FOR DEPTH FIRST SEARCH
    private void dfsVisit(int index, boolean[] visited) {
        visited[index] = true;
        System.out.println("Visited:[ Name: " + contactsBook[index].getName() + " | Student ID: " + contactsBook[index].getStudentId() + " ]");

        for (int i = 0; i < maxSize; i++) {
            if (matrix[index][i] == 1 && !visited[i]) {
                dfsVisit(i, visited);
            }
        }
    }

}