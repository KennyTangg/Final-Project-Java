package com.contactsmanager;

import com.contactsmanager.interfaces.Graph;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Graph<String> contactsBook = new Graph<>(true);

        contactsBook.addNode("Bob");
        contactsBook.addNode("May");
        contactsBook.addNode("Hugh");
        contactsBook.addNode("Don");
        contactsBook.addNode("April");

        contactsBook.printContact();

    }
}