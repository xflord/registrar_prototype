package org.perun.registrarprototype.models;

import java.util.Set;

public enum ItemType {
  // TODO see whether there's an existing library for form design, also consider custom CSS styling for forms (can it break the whole design, injection, etc.)
    ROW,
    SECTION,
    SUBMIT_BUTTON,
    DATE_PICKER,
    LOGIN,
    PASSWORD,
    VERIFIED_EMAIL,
    HTML_COMMENT,
    TEXTFIELD;

    public static final Set<ItemType> HTML_ITEMS = Set.of(HTML_COMMENT);
    public static final Set<ItemType> UPDATABLE_ITEMS = Set.of(TEXTFIELD, DATE_PICKER, VERIFIED_EMAIL);
    public static final Set<ItemType> VERIFIED_ITEMS = Set.of(VERIFIED_EMAIL);
    public static final Set<ItemType> LAYOUT_ITEMS = Set.of(ROW, SECTION, SUBMIT_BUTTON, HTML_COMMENT);
    public static final Set<ItemType> SUBMIT_ITEMS = Set.of(SUBMIT_BUTTON);

    public boolean isUpdatable() {
      return UPDATABLE_ITEMS.contains(this);
    }

    public boolean isHtmlItem() {
      return HTML_ITEMS.contains(this);
    }

    public boolean isVerifiedItem() {
      return VERIFIED_ITEMS.contains(this);
    }

    public boolean isLayoutItem() {
      return LAYOUT_ITEMS.contains(this);
    }

    public boolean isSubmitItem() {
      return SUBMIT_ITEMS.contains(this);
    }
}
