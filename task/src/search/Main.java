package search;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class InvertedIndex {
    public HashMap<String, HashSet<Integer>> invertedIndex;
    public InvertedIndex(ArrayList<String> data) {
        this.createInvertedIndex(data);
    }
    public void createInvertedIndex(ArrayList<String> data) {
        this.invertedIndex = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i).split(" ");
            for (String word : row) {
                String current = word.toLowerCase();
                if (this.invertedIndex.containsKey(current)) {
                    this.invertedIndex.get(current).add(i);
                } else {
                    this.invertedIndex.put(current, new HashSet<>(Set.of(i)));
                }
            }
        }
    }
}
interface SearchMethod {
    HashSet<Integer> search(String query, InvertedIndex invertedIndex);
}

class Searcher {

    private SearchMethod method;

    public Searcher() {
    }

    public Searcher(SearchMethod method) {
        this.method = method;
    }

    public HashSet<Integer> search(String query, InvertedIndex invertedIndex) {
        return this.method.search(query, invertedIndex);
    }

    public void setMethod(SearchMethod method) {
        this.method = method;
    }

    public SearchMethod getMethod() {
        return this.method;
    }
}

class SearchAny implements SearchMethod {
    @Override
    public HashSet<Integer> search(String query, InvertedIndex invertedIndex) {
        var indexes = new HashSet<Integer>();
        var inverted = invertedIndex.invertedIndex;
        String[] words = query.split(" ");
        Arrays.stream(words).forEach(word -> {
            if (inverted.get(word) != null) {
                indexes.addAll(inverted.get(word));
            }
        });
        return indexes;
    }
}

class SearchNone implements SearchMethod {
    @Override
    public HashSet<Integer> search(String query, InvertedIndex invertedIndex) {
        String[] words = query.split(" ");
        var indexes = new HashSet<Integer>();
        invertedIndex.invertedIndex.forEach((key, value) ->  indexes.addAll(value));
        Arrays.stream(words).forEach(word -> invertedIndex.invertedIndex.forEach((key, value) -> {
            if (key.equals(word)) {
                indexes.removeAll(value);
            }
        }));
        return indexes;
    }
}

class SearchAll implements SearchMethod {
    @Override
    public HashSet<Integer> search(String query, InvertedIndex invertedIndex) {

        String[] words = query.split(" ");
        var invert = invertedIndex.invertedIndex;
        var indexes = new HashSet<Integer>();

        // check if first word is in inverted
        if (!invert.containsKey(words[0])) {
            return indexes;
        }

        indexes.addAll(invert.get(words[0]));

        for ( String word: words) {
            if (!invert.containsKey(word)) {
                indexes.clear();
                break;
            }
            var currentIndexes = invert.get(word);
            indexes.removeIf(index -> !currentIndexes.contains(index));
        }
        return indexes;
    }
}

public class Main {
    public static void main(String[] args) {

        ArrayList<String> people = readFile(args[1]);
        var invertedIndex = new InvertedIndex(people);
        Scanner scanner = new Scanner(System.in);
        Searcher searcher = new Searcher();
        String enteredOption = "";
        try {
            while (!enteredOption.equals("0")) {
                printMenu();
               enteredOption = scanner.next();
                if (enteredOption.equals("1")) {
                    String strategy = scanner.next();
                    scanner.nextLine();
                    String query = scanner.nextLine().toLowerCase();
                    setStrategy(strategy, searcher);
                    if (searcher.getMethod() != null) {
                        var indexes = searcher.search(query, invertedIndex);
                        if (!indexes.isEmpty()) {
                            indexes.forEach(index -> System.out.println(people.get(index)));
                        }
                    }
                } else if (enteredOption.equals("2")) {
                    printAllPeople(people);
                } else {
                    System.out.println("Incorrect option! Try again.");
                }
            }
            System.out.println("Bye");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    public static void search(String query, HashMap<String, TreeSet<Integer>> invertedIndex, ArrayList<String> data) {
//        var result = invertedIndex.getOrDefault(query, new TreeSet<>());
//        System.out.println(result);
//        if (result.size() != 0) {
//            result.forEach(index -> System.out.println(data.get(index)));
//        } else {
//            System.out.println("Not found");
//        }
//    }

//    public static HashMap<String, HashSet<Integer>> createInvertedIndex(ArrayList<String> data) {
//        var invertIndex = new HashMap<String, HashSet<Integer>>();
//        for (int i = 0; i < data.size(); i++) {
//            String[] row = data.get(i).split(" ");
//            for (String word : row) {
//                String current = word.toLowerCase();
//                if (invertIndex.containsKey(current)) {
//                    invertIndex.get(current).add(i);
//                } else {
//                    invertIndex.put(current, new HashSet<>(Set.of(i)));
//                }
//            }
//        }
//        return invertIndex;
//    }

    public static ArrayList<String> readFile(String path) {
        ArrayList<String> data = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNext()) {
                data.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found");
        }
        return data;
    }

    /**
     * Print all people
     *
     * @param people array contains all people
     */
    public static void printAllPeople(ArrayList<String> people) {
        assert people.size() > 0 : "Empty array, printing all people";
        for (String person : people) {
            System.out.println(person);
        }
    }

    /**
     * Print menu
     */
    public static void printMenu() {
        System.out.println("""
                === Menu ===
                1. Find a person
                2. Print all people
                0. Exit
                """);
    }

    public static void setStrategy(String strategy, Searcher searcher) {
        switch (strategy.toLowerCase()) {
            case "any" -> searcher.setMethod(new SearchAny());
            case "all" -> searcher.setMethod(new SearchAll());
            case "none" -> searcher.setMethod(new SearchNone());
            default -> System.out.println("Please select all, any or none");
        }

    }

}
