package com.contactsmanager.DataStructures;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Graph implements ContactsManager{
    private Map<Contact, LinkedList<Contact>> adj = new HashMap<>();
    private boolean directed;

    public Graph() {
        directed = false; // Default is undirected
    }

    public Graph(boolean d) {
        directed = d; // Can be true or false
    }

    // ADD NODE -OK
    @Override
    public void addContact(Contact contact) {
        adj.putIfAbsent(contact, new LinkedList<>());
    }

    // SEARCH NODE -OK
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

    // UPDATE CONTACT -OK
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        Contact target = searchContact(contact.getName());
        if (target != null) {
            contact.setName(newName);
            contact.setStudentId(newStudentId);
        }
        //FIXED: need to change other people too
        // Update all adjacency lists that reference the old contact
        for (Map.Entry<Contact, LinkedList<Contact>> entry : adj.entrySet()) {
            LinkedList<Contact> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(contact)) {
                    list.set(i, target); // Point to updated object
                }
            }
        }

    }

    // ADD CONNECTION -OK
    @Override
    public void addConnection(String contact1, String contact2) {
        // Validate input
        if (contact1 == null || contact2 == null) {
            System.out.println("Contact names cannot be null");
            return;
        }

        // Search for the contacts
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
        }
        neighborsOfA.add(nodeB); // Add edge a->b

        // If undirected, add the reverse connection
        if (!directed) {
            LinkedList<Contact> neighborsOfB = adj.get(nodeB);
            if (neighborsOfB == null) {
                neighborsOfB = new LinkedList<>();
                adj.put(nodeB, neighborsOfB);
            }
            neighborsOfB.add(nodeA); // Add edge b->a
        }

        System.out.println("Connection added between " + contact1 + " and " + contact2);
    } // Time O(V), Space O(1)


    // DELETE CONNECTION -OK
    @Override
    public void removeConnection(String contact1, String contact2) {
        Contact nodeA = searchContact(contact1); // Search nodes
        Contact nodeB = searchContact(contact2);
        if (nodeA == null || nodeB == null) {
            System.out.println("Some nodes are missing/ doesn't exist.");
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

    // DELETE NODE - OK
    /*To delete a node,
    you must first remove all edges connected to the node
    and then remove the node from the key set of adj. */
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
            System.out.println("Contact not found: " + name);
            return;
        }

        if (!directed) { // Undirected graph
            LinkedList<Contact> neighbors = adj.get(target);
            for (Contact node : neighbors) {
                adj.get(node).remove(target); // Remove edges per person
            }
        } else {
            for (Contact contact : adj.keySet()) { // Remove edges to the contact for whole contact list
                adj.get(contact).remove(target);
            }
        }

        adj.remove(target); // Remove the contact itself
        System.out.println("Deleted contact: " + name);
    } // Time O(V+E), Space O(1),
    // V is the number of nodes,
    // E is the number of edges

    // TRAVERSAL DEPTH FIRST SEARCH
    /* The variable visited is used to ensure each node is visited only once.
    The technique is called memoization,
    caching the result in a variable to prevent redundant computations.
    It resolves the issue of overlapping in recursion.*/
    public void dfsTraversal(Contact start) {
        if (!adj.containsKey(start)) {
            return;
        }
        HashMap<Contact, Boolean> visited = new HashMap<>();
        dfs(start, visited); // Call recursive method
    }

    private void dfs(Contact v, HashMap<Contact, Boolean> visited) {
        visited.put(v, true);
        System.out.print(v.toString() + " ");
        for (Contact neighbor : adj.get(v)) {
            if (visited.get(neighbor) == null) { // All neighbors
                dfs(neighbor, visited); // Call itself
            }
        }
    } // Time O(V+E), Space O(V)
    // V is the number of nodes,
    // E is the number of edges

    // TRAVERSAL BREADTH FIRST SEARCH utilizing a QUEUE
    /* when searching in a large graph, BFS is preferred.
    The BFS algorithm searches the nodes close to the starting point first,
    making it ideal for finding the shortest path.*/
    public void bfsTraversal(Contact start) {
        if (!adj.containsKey(start)) {
            return;
        }
        Queue<Contact> queue = new LinkedList<>();
        queue.add(start); // Enqueue root
        HashMap<Contact, Boolean> visited = new HashMap<>();
        visited.put(start, true);
        while (!queue.isEmpty()) {
            Contact v = queue.poll(); // Dequeue
            System.out.print(v.toString() + " ");
            for (Contact neighbor : adj.get(v)) {
                if (visited.get(neighbor) == null) {
                    queue.add(neighbor); // Enqueue neighbors
                    visited.put(neighbor, true);
                }
            }
        }
    } // Time O(V+E), Space O(V) used for queue
    // V is the number of nodes,
    // E is the number of edges

    // PRINT CONTACT
    public void printContact() {
        for (Contact node : adj.keySet()) {
            System.out.print(node.toString() + ": ");
            System.out.println(adj.get(node));
        }
    }

    // RETURN ALL CONTACTS -OK
    @Override
    public List<Contact> listAllContacts() {
        List<Contact> allContacts = new LinkedList<>();
        for (Contact node : adj.keySet()) {
            allContacts.add(node);
        }
        return allContacts;
    }

    // SUGGEST CONTACTs -OK
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

        return recommendedContacts;
    }

}
