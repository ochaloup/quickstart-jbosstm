package org.jboss.narayana.quickstarts.jta;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MyMain {
public static void main(String[] args) {
    try {
        new InitialContext().lookup("java:/test");
    } catch (NamingException ne) {
        throw new RuntimeException(ne);
    }
}
}
