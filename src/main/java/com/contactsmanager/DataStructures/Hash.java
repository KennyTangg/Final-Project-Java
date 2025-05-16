package com.contactsmanager.DataStructures;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

public class Hash implements ContactsManager {

    private Map<Contact, List<Contact>> hash = new HashMap<>();


    public Hash() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void addContact(Contact contact) {
        if (hash.containsKey(contact)) {
            System.out.println("Already Exists");
            return;
        }

        hash.put(contact, new ArrayList<>());

    }

    @Override
    public Contact searchContact(String name) {
        //Di hashmap -> find a contact with a specific name

        System.out.println("Searching for contact name: " + name);

        for (Contact temp : hash.keySet()) {
            //For each key (Contact) in the hashmap, we iterate using a temporary variable (Contact datatype as well)
            if (temp.getName().toLowerCase().equals(name.toLowerCase())) {
                System.out.println("Contact " + name + " Found");
                return temp;
            }
        }
        System.out.println("Contact " + name + " Not Found");

        return null;
    }

    @Override
    public void deleteContact(String name) {
        System.out.println("Trying to delete contact with the name: " + name);

        //validate if contact exists
        Contact contactToDelete = searchContact(name);
        if (contactToDelete == null) {
            return;
        }

        //Remove said contact from HashMap
        hash.remove(contactToDelete);
        System.out.println("Contact with the name: " + name + " has been deleted.");

        //Remove the connections that the previous contact had
        System.out.println("Removing from connections.");
        for (List<Contact> temp : hash.values()) {
            temp.remove(contactToDelete);
        }


    }

    @Override
    public void updateContact(Contact contactToUpdate, String newName, int newStudentId) {
        System.out.println("Updating contact.");

        //validate if contact exists
        if (!hash.containsKey(contactToUpdate)) {
            System.out.println("Contact not found.");
            return;
        }

        hash.remove(contactToUpdate);
        Contact newContact = new Contact(newName, newStudentId);
        List<Contact> oldConnections = hash.get(contactToUpdate);
        hash.put(newContact, oldConnections);

        //Update for each existing connnections
        for (List<Contact> temp : hash.values()) { //iterate our hashmap, access each connections

            for (int i = 0; i < temp.size(); i++) { //iterate each connections list, find the old data

                if (temp.get(i).equals(contactToUpdate)) {//update with new data
                    temp.set(i, newContact);
                }
            }
        }
    }

    @Override
    public List<Contact> listAllContacts() {
        return new ArrayList<>(hash.keySet());
    }

    @Override
    public void addConnection(String contact1, String contact2) {
        Contact c1 = searchContact(contact1);
        Contact c2 = searchContact(contact2);

        if (c1 == null || c2 == null) {
            System.out.println("One or both contacts are null");
        }

        if (!hash.get(c1).contains(c2)) {
            hash.get(c1).add(c2);
        }

        if (!hash.get(c2).contains(c1)) {
            hash.get(c2).add(c1);
        }
    }

    @Override
    public void removeConnection(String contact1, String contact2) {
        Contact c1 = searchContact(contact1);
        Contact c2 = searchContact(contact2);

        if (c1 == null || c2 == null) {
            System.out.println("One or both contacts are null");
        }

        hash.get(c1).remove(c2);
        hash.get(c2).remove(c1);
    }

    @Override
    public List<Contact> suggestContacts(String contact) {
        List<Contact> suggestions = new ArrayList<>();

        Contact currentUser = searchContact(contact);
        if (currentUser == null) {
            System.out.println("Contact not found: " + contact);
            return suggestions;
        }

        Set<Contact> direct = new HashSet<>(hash.get(currentUser));

        if (direct.isEmpty()) {
            System.out.println("Unable to suggest contacts from not knowing anyone.");
            return suggestions;
        }

        Set<Contact> seen = new HashSet<>(direct);

        for (Contact friend : direct) {
            for (Contact friendOfFriend : hash.get(friend)) {

                if (!seen.contains(friendOfFriend) && !suggestions.contains(friendOfFriend)) {
                    suggestions.add(friendOfFriend);
                }

            }
        }
        return suggestions;
    }

}

