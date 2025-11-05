package org.perun.registrarprototype.services.idmIntegration.perun;

import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class NameParser {

  private static final Pattern TITLE_BEFORE_PATTERN = Pattern.compile("^(?:\\p{L}+\\.|et)$");
  private static final Pattern FIRST_NAME_PATTERN   = Pattern.compile("^[\\p{L}'-]+$");
  private static final Pattern TITLE_AFTER_PATTERN  = Pattern.compile("^\\p{L}+\\.$");

  // Common last-name particles (case-insensitive)
  private static final Set<String> LAST_NAME_PARTICLES = Set.of(
          "da", "de", "del", "della", "der", "di", "dos", "du", "la", "le", "van", "von"
  );

  // Common suffixes that belong with the last name
  private static final Set<String> NAME_SUFFIXES = Set.of("jr", "sr", "ii", "iii", "iv", "v");

  public record ParsedName(
      String titleBefore,
      String firstName,
      String middleName,
      String lastName,
      String titleAfter
  ) {}

  public static ParsedName parseDisplayName(String displayName) {
    if (StringUtils.isEmpty(displayName)) {
      return null;
    }

    // Normalize
    String normalized = displayName
        .replaceAll("[,_]", " ")
        .replaceAll("\\s+", " ")
        .strip();

    List<String> parts = new ArrayList<>(Arrays.asList(normalized.split(" ")));

    String titleBefore = extract(parts, TITLE_BEFORE_PATTERN, true);
    String firstName   = extract(parts, FIRST_NAME_PATTERN, false);

    List<String> trailingTitles = new ArrayList<>();
    ListIterator<String> reverse = parts.listIterator(parts.size());
    while (reverse.hasPrevious()) {
      String part = reverse.previous();
      if (TITLE_AFTER_PATTERN.matcher(part).matches()) {
          trailingTitles.addFirst(part);
          reverse.remove();
      } else {
          break;
      }
    }
    String titleAfter = trailingTitles.isEmpty() ? null : String.join(" ", trailingTitles);

    String middleName = null;
    String lastName = null;

    if (parts.size() == 1) {
      lastName = parts.getFirst();

    } else if (parts.size() >= 2) {
      // Check for suffixes (Jr., Sr., II...) and keep them with last name
      String lastToken = parts.getLast();
      boolean hasSuffix = NAME_SUFFIXES.contains(lastToken.replace(".", "").toLowerCase());

      // Start by assuming last name = last part (+ suffix if applicable)
      int lastNameStartIndex = parts.size() - (hasSuffix ? 2 : 1);

      // If preceding tokens are particles, include them in the last name
      while (lastNameStartIndex > 0) {
          String maybeParticle = parts.get(lastNameStartIndex - 1).toLowerCase();
          if (LAST_NAME_PARTICLES.contains(maybeParticle)) {
              lastNameStartIndex--;
          } else {
              break;
          }
      }

      lastName = String.join(" ", parts.subList(lastNameStartIndex, parts.size()));
      // if anything is left, consider it middle name
      if (lastNameStartIndex > 1) {
        middleName = String.join(" ", parts.subList(0, lastNameStartIndex));
      } else if (lastNameStartIndex == 1) {
        middleName = parts.getFirst();
      }
    }
    return new ParsedName(titleBefore, firstName, middleName, lastName, titleAfter);
  }

  private static String extract(List<String> parts, Pattern pattern, boolean greedy) {
    StringJoiner joiner = new StringJoiner(" ");
    Iterator<String> it = parts.iterator();
    while (it.hasNext()) {
        String part = it.next();
        if (pattern.matcher(part).matches()) {
            joiner.add(part);
            it.remove();
            if (!greedy) break;
        } else if (!joiner.toString().isEmpty()) {
            break;
        }
    }
    String result = joiner.toString().strip();
    return result.isEmpty() ? null : result;
  }
}
