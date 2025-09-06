package org.perun.registrarprototype.models;

// value object, can be a record
public record ValidationError(int itemId, String message) {
}
