package com.github.learntocode2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Book {
  private final String title;
  private final String author;
  private String publisher;
  private int pageCount;
  private final double price;
  // A new field was added as part of evolving requirements
  private final String edition;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Book)) {
      return false;
    }
    if (!this.title.equals(((Book) other).getTitle())) {
      return false;
    }
    return this.author.equals(((Book) other).getAuthor());
  }

  public int hashCode() {
    return Objects.hash(getAuthor(), getTitle());
  }

  public static void main(String[] args){
    searchBookByTitleAndAuthor();
    searchBookEditionsByTitleAndAuthor();
  }

  private static void searchBookEditionsByTitleAndAuthor() {
    var library = new HashMap<TitleAuthor, List<Book>>();
    var searchKey = new TitleAuthor("The Pragmatic Programmer", "Dave Thomas");
    library.computeIfAbsent(searchKey, k -> new ArrayList<Book>())
        .add(new Book("The Pragmatic Programmer", "Dave Thomas", 2000, "First"));
    library.computeIfAbsent(searchKey, k -> new ArrayList<Book>())
        .add(new Book("The Pragmatic Programmer", "Dave Thomas", 2000, "Second"));
    var secondKey = new TitleAuthor("Microservices Patterns", "Chris Richardson");
    library.computeIfAbsent(secondKey, k -> new ArrayList<Book>())
        .add(new Book("Microservices Patterns", "Chris Richardson", 2000, "First"));
    assert library.get(searchKey).size() == 2;
  }

  private static void searchBookByTitleAndAuthor() {
    var searchKey = new TitleAuthor("The Pragmatic Programmer", "Dave Thomas");

    var library = new HashMap<TitleAuthor, Book>();
    library.put(
        new TitleAuthor("Microservices Patterns", "Chris Richardson"),
        new Book("Microservices Patterns", "Chris Richardson", 2000, "First"));
    library.put(
        searchKey,
        new Book("The Pragmatic Programmer", "Dave Thomas", 2000, "First"));
    library.put(
        searchKey,
        new Book("The Pragmatic Programmer", "Dave Thomas", 2000, "Second"));

    assert library.containsKey(searchKey);
  }

  // Composite key
  record TitleAuthor(String title, String author) {}
}
