package com.contactsmanager.contactsmanagerfx.dataStructures;

import com.contactsmanager.contactsmanagerfx.interfaces.ConnectionsManager;
import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * This class is a Contacts Book that is implemented using Graph (made from Adjacency List).
 * It manages Contact nodes and their directed or undirected connections.
 * CB stands for Contacts Book.
 */
public class AdjacencyListGraphCB implements ContactsManager, ConnectionsManager{

    private final Map<Contact, LinkedList<Contact>> adj = new HashMap<>(); // Where our data is stored
    private final boolean directed;

    /**
     * Constructs an undirected contact graph.
     */
    public AdjacencyListGraphCB() {
        directed = false; // Default is undirected
    }

    /**
     * Constructs a contact graph with directionality defined.
     * @param directed Directed graph or not
     */
    public AdjacencyListGraphCB(boolean directed) {
        this.directed = directed; // Can be true or false
    }

    /*========================================================================*/
    /*===== Contacts/Node Management =========================================*/

    // ADD NODE
    /**
     * {@inheritDoc}
     *
     * @implSpec Ensures no duplicate names (case-insensitive) exist before adding.
     * The new contact is added with an empty list of connections.
     */
    @Override
    public void addContact(Contact contact) {

        for (Contact existing : adj.keySet()) { // To prevent duplicate names
            if (existing.getName().trim().equalsIgnoreCase(contact.getName().trim())) {
                System.out.println("Contact with name '" + contact.getName() + "' already exists. Failed to put in contact.");
                return;
            }
        }

        adj.putIfAbsent(contact, new LinkedList<>());
        System.out.println("Added contact. Name: '" + contact.getName() + "' | Student ID: " + contact.getStudentId() +".");
    }

    // UPDATE CONTACT
    /**
     * {@inheritDoc}
     *
     * @implSpec Since Contact is used as a key in the map, this method removes the old entry,
     * updates the Contact fields, and reinserts the object to maintain consistency. It also
     * updates any connection lists pointing to the original object.
     */
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        Contact oldKey = null;
        for (Contact c : adj.keySet()) {
            if (c.getName().equalsIgnoreCase(contact.getName())) {
                oldKey = c;
                break;
            }
        }

        if (oldKey == null) {
            System.out.println("Contact not found.");
            return;
        }

        // Store and transfer connections
        LinkedList<Contact> connections = adj.get(oldKey);
        adj.remove(oldKey);

        Contact newContact = new Contact(newName, newStudentId);
        adj.put(newContact, connections);

        // Replace reference in other contact's connection lists
        for (LinkedList<Contact> list : adj.values()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(oldKey)) {
                    list.set(i, newContact);
                }
            }
        }
    }


    // DELETE NODE
    /*To delete a node,
    you must first remove all edges connected to the node
    and then remove the node from the key set of adj. */
    /**
     * {@inheritDoc}
     *
     * @implSpec Deletes the contact and removes all associated edges.
     * In undirected mode, connections from other contacts are also removed.
     */
    @Override
    public void deleteContact(String name) {
        Contact target = null;
        for (Contact contact : adj.keySet()) { // Find the contact with the given name
            if (contact.getName().equals(name)) {
                target = contact;
                break;
            }
        }
        if (target == null) {
            System.out.println("Contact not found: '" + name + "'. Deletion unsuccessful.");
            return;
        }

        if (!directed) {
            LinkedList<Contact> neighbors = adj.get(target);
            if (neighbors != null) {
                List<Contact> neighborsCopy = new LinkedList<>(neighbors); // Avoid concurrent modification
                for (Contact node : neighborsCopy) {
                    Contact resolvedNode = searchContact(node.getName()); // Normalize
                    if (resolvedNode != null) {
                        LinkedList<Contact> theirList = adj.get(resolvedNode);
                        if (theirList != null) {
                            theirList.remove(target); // target is the actual object to remove
                        }
                    }
                }
            }
        } else {
            for (Contact contact : adj.keySet()) {
                List<Contact> theirList = adj.get(contact);
                if (theirList != null) {
                    theirList.remove(target); // Remove edges to the contact for whole contact list
                }
            }
        }
        adj.remove(target);
        System.out.println("Deleted contact: " + name);
    }

    // SEARCH NODE
    /**
     * {@inheritDoc}
     */
    @Override
    public Contact searchContact(String name) {
        for (Contact contact : adj.keySet()) { // Find contact
            if (contact.getName().equals(name)) {
                return contact;
            }
        }
        System.out.println("Contact not found: " + name);
        return null;
    }

    /*========================================================================*/
    /*===== Connections Management ===========================================*/

    // ADD CONNECTION
    /**
     * {@inheritDoc}
     *
     * @implSpec Adds a one-way or two-way connection depending on directionality.
     * Prevents duplicate connections.
     * @throws IllegalArgumentException if contact1 or contact2 is null
     */
    @Override
    public void addConnection(String contact1, String contact2) {

        // Validate input
        if (contact1 == null || contact2 == null) {
            System.out.println("Contact names cannot be null");
            return;
        }

        Contact nodeA = searchContact(contact1);
        Contact nodeB = searchContact(contact2);

        // Check if both contacts exist
        if (nodeA == null || nodeB == null) {
            System.out.println("Some nodes are missing/ doesn't exist.");
            return; // Return early if either contact is not found
        }

        // Check if the contacts are in the adjacency map
        if (!adj.containsKey(nodeA) || !adj.containsKey(nodeB)) {
            System.out.println("Some nodes are not in the graph.");
            return;
        }

        // Add the connection
        LinkedList<Contact> neighborsOfA = adj.get(nodeA);
        if (neighborsOfA == null) {
            neighborsOfA = new LinkedList<>();
            adj.put(nodeA, neighborsOfA);
        } else if (neighborsOfA.contains(nodeB)) {
            System.out.println("Connection between " + contact1 + " and " + contact2 + " already exists.");
            return;
        } else { // Only if it doesn't contain the connection already
            neighborsOfA.add(nodeB); // Add edge a->b
            System.out.println("Connection added between " + contact1 + " and " + contact2);
        }

        // If undirected graph
        if (!directed) {
            LinkedList<Contact> neighborsOfB = adj.get(nodeB);
            if (neighborsOfB == null) {
                neighborsOfB = new LinkedList<>();
                adj.put(nodeB, neighborsOfB);
            } else if (!neighborsOfB.contains(nodeA)) { // Only if it doesn't contain the connection already
                neighborsOfB.add(nodeA); // Add edge b->a
            }
        }
    }

    // DELETE CONNECTION
    /**
     * {@inheritDoc}
     *
     * @implSpec Deletes a one-way or two-way connection depending on directionality.
     * @throws IllegalArgumentException if contact1 or contact2 is null
     */
    @Override
    public void removeConnection(String contact1, String contact2) {
        Contact nodeA = searchContact(contact1); // Search nodes
        Contact nodeB = searchContact(contact2);
        if (nodeA == null || nodeB == null) {
            System.out.println("Some nodes are missing or doesn't exist.");
            return;
        }

        LinkedList<Contact> neighborsOfA = adj.get(nodeA); // Get neighbors
        LinkedList<Contact> neighborsOfB = adj.get(nodeB);
        if (neighborsOfA == null || neighborsOfB == null) {
            System.out.println("Connection doesn't exist."); // No neighbors found
            return;
        }
        neighborsOfA.remove(nodeB); // Remove a->b
        if (!directed) { // Undirected graph
            neighborsOfB.remove(nodeA); // Remove b->a
        }
    }

    // SUGGEST CONTACTS
    /**
     * {@inheritDoc}
     *
     * @implSpec Will not suggest the user itself. Will recommend the person's friends' friends.
     */
    @Override
    public List<Contact> suggestContacts(String contact) {
        List<Contact> recommendedContacts = new LinkedList<>();
        Contact nodeA = searchContact(contact);

        if (nodeA == null) {
            System.out.println("Contact not found: " + contact);
            return recommendedContacts;
        }

        LinkedList<Contact> directConnections = adj.get(nodeA);
        if (directConnections == null || directConnections.isEmpty()) {
            System.out.println("Unable to suggest contacts from not knowing anyone.");
            return recommendedContacts;
        }

        // For fast lookup
        HashMap<Contact, Boolean> isDirectFriend = new HashMap<>();
        for (Contact c : directConnections) {
            isDirectFriend.put(c, true);
        }
        isDirectFriend.put(nodeA, true); // Avoid suggesting self

        for (Contact friend : directConnections) { // For each direct friend
            LinkedList<Contact> friendsOfFriend = adj.get(friend);
            if (friendsOfFriend != null) {
                for (Contact potential : friendsOfFriend) { // Direct friends' friends
                    if (!isDirectFriend.containsKey(potential) && !recommendedContacts.contains(potential) && !potential.equals(nodeA)) { // Avoid duplicates
                        recommendedContacts.add(potential);
                    }
                }
            }
        }

        if (recommendedContacts.isEmpty()) {
            System.out.println(contact + "'s friends don't know anyone.");
        }

        return recommendedContacts;
    }

    /*========================================================================*/
    /*===== Printing and Getters Management ==================================*/

    // PRINT ALL CONTACTS
    /**
     * Print the contacts list on the terminal.
     */
    public void printContactsBook() {
        for (Contact node : adj.keySet()) {
            System.out.print(node.toString() + ": ");
            System.out.println(adj.get(node));
        }
    }

    // RETURN ALL CONTACTS
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> listAllContacts() {
        List<Contact> allContacts = new LinkedList<>();
        for (Contact node : adj.keySet()) {
            allContacts.add(node);
        }
        return allContacts;
    }

    // RETURN THE ADJACENCY LIST
    /**
     * Getter for the Adjacency List.
     * @return the adjacency list in Map<Contact, LinkedList<Contact>>
     */
    public Map<Contact, LinkedList<Contact>> getAdjacencyList() {
        return adj;
    }

    /*========================================================================*/
    /*===== Traversal Management =============================================*/

    // TRAVERSAL: BREADTH FIRST SEARCH utilizing a QUEUE
    // The BFS algorithm searches nodes close to the start point first.
    // Ideal for shortest-path problems in unweighted graphs.
    /**
     * Breadth first search traversal that abides to one-way connections.
     * @param contact The name of the contact to start from
     */
    public void bfsTraversal(String contact) {
        Contact start = searchContact(contact);

        if (!adj.containsKey(start)) { // If the start key doesn't exist
            System.out.println("Start contact does not exist in this graph.");
            return;
        }

        Queue<Contact> queue = new LinkedList<>();
        HashMap<Contact, Boolean> visited = new HashMap<>(); // Where we store whether something has been visited.

        queue.add(start); // Enqueue root
        visited.put(start, true);

        while (!queue.isEmpty()) {
            Contact visiting = queue.poll(); // Dequeue
            System.out.println("Visited:[ Name: " + visiting.getName().trim() + " | Student ID: " + visiting.getStudentId() + " ]");

            for (Contact neighbor : adj.get(visiting)) {
                if (visited.get(neighbor) == null) {
                    queue.add(neighbor); // Enqueue neighbors
                    visited.put(neighbor, true);
                }
            }
        }
    }

    // TRAVERSAL: DEPTH FIRST SEARCH
    // The variable visited is used to ensure each node is visited only once.
    // Uses memoization, caching the result in a variable to prevent redundant computations.
    /**
     * Depth first search traversal that abides to one-way connections.
     * @param contact The name of the contact to start from
     */
    public void dfsTraversal(String contact) {

        Contact start = searchContact(contact);
        if (!adj.containsKey(start)) {
            System.out.println("Start contact does not exist in this graph.");
            return;
        }
        HashMap<Contact, Boolean> visited = new HashMap<>();
        dfs(start, visited); // Call recursive method
    }

    private void dfs(Contact visiting, HashMap<Contact, Boolean> visited) {
        visited.put(visiting, true);
        System.out.println("Visited:[ Name: " + visiting.getName().trim() + " | Student ID: " + visiting.getStudentId() + " ]");

        for (Contact neighbor : adj.get(visiting)) {
            if (visited.get(neighbor) == null) { // All neighbors
                dfs(neighbor, visited); // Call itself
            }
        }
    }

}
