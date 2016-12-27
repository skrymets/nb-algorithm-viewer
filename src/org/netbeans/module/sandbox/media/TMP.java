/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.sandbox.media;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.openide.util.Exceptions;

/**
 *
 * @author Lot
 */
public class TMP {

    public static void main(String[] args) {

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (int i = 0; i < 10; i++) {
                digest.update(UUID.randomUUID().toString().getBytes());

                System.out.println(digest.toString());

                digest.reset();
            }
        } catch (NoSuchAlgorithmException ex) {
            Exceptions.printStackTrace(ex);
        }

//        Graph g = new Graph();
//        Node<String> head = g.createNode("6af806b85123");
//        Node<String> tail
//                = head
//                .connect(g.createNode("998086cce01f")).getRight()
//                .connect(g.createNode("06dcecfb6b77")).getRight()
//                .connect(g.createNode("a5b4fbdf93a7")).getRight();
    }

}
