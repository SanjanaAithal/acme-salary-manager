package org.example.acmesalarymanager.employee;

public class InvalidSortException extends RuntimeException {

    public InvalidSortException(String property) {
        super("Cannot sort by '" + property + "'");
    }
}