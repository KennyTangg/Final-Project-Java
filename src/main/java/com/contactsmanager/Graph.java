package com.contactsmanager;

import com.contactsmanager.interfaces.Graph;
import com.contactsmanager.model.Contact;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Graph {
    private Map<Contact, LinkedList<Contact>> adj = new HashMap<>();
    private boolean directed;

    public Graph() {
        directed = false; // Default is undirected
    }

    public Graph(boolean d) {
        directed = d; // Can be true or false
    }

    // ADD NODE
    public void addNode(Contact nodeA) {
        adj.putIfAbsent(nodeA, new LinkedList<>());
    }

    // ADD CONNECTION (and NODE if any is missing)
    public void addEdge(Contact nodeA, Contact nodeB) {
        adj.putIfAbsent(nodeA, new LinkedList<>()); // Add node a
        adj.putIfAbsent(nodeB, new LinkedList<>()); // Add node b
        adj.get(nodeA).add(nodeB); // Add edge a->b
        if (!directed) {
            adj.get(nodeB).add(nodeA); // Add edge b->a
        }
    } // Time O(1), Space O(1)

    // DELETE CONNECTION
    public boolean removeEdge(Contact nodeA, Contact nodeB) {
        if (!adj.containsKey(nodeA) || !adj.containsKey(nodeB)) {
            return false; // Invalid input
        }
        LinkedList<Contact> neighborsOfA = adj.get(nodeA);
        LinkedList<Contact> neighborsOfB = adj.get(nodeB);
        if (neighborsOfA == null || neighborsOfB == null) {
            return false; // No neighbors found
        }
        boolean r1 = neighborsOfA.remove(nodeB); // Remove a->b
        if (!directed) { // Undirected graph
            boolean r2 = neighborsOfB.remove(nodeA); // Remove b->a
            return r1 && r2;
        }
        return r1;
    } // Time O(1), Space O(1)

    // DELETE NODE
    /*To delete a node,
    you must first remove all edges connected to the node
    and then remove the node from the key set of adj. */
    public boolean removeNode(Contact nodeA) {
        if (!adj.containsKey(nodeA)) {
            return false;
        }
        if (!directed) { // Undirected graph
            LinkedList<Contact> neighbors = adj.get(nodeA);
            for (Contact node : neighbors) {
                adj.get(node).remove(nodeA); // Remove edges
            }
        } else {
            for (Contact key : adj.keySet()) {
                adj.get(key).remove(nodeA); // Remove edges
            }
        }
        adj.remove(nodeA); // Remove node
        return true;
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
            System.out.print(node.toString() + ":");
            System.out.print(adj.get(node).toString());
        }
    }
}
