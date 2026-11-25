import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Chat Application Simulator
 * Data Structures: Queue, Linked List, Stack, Hashing (HashMap)
 * Run: javac ChatApplicationSimulator.java
 *      java ChatApplicationSimulator
 */
public class ChatApplicationSimulator {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, User> users = new HashMap<>();
    private static final Queue<Message> incomingQueue = new LinkedList<>();
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    static class Message {
        final String sender;
        final String receiver;
        final String text;
        final String time;

        Message(String sender, String receiver, String text) {
            this.sender = sender;
            this.receiver = receiver;
            this.text = text;
            this.time = LocalTime.now().format(TIME_FORMAT);
        }

        @Override
        public String toString() {
            return "[" + time + "] " + sender + " -> " + receiver + ": " + text;
        }
    }

    static class User {
        final String username;
        // LinkedList stores this user's chat history.
        final LinkedList<Message> history = new LinkedList<>();
        // Stack stores messages sent by this user for the Undo feature.
        final Deque<Message> sentStack = new ArrayDeque<>();

        User(String username) {
            this.username = username;
        }
    }

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("       CHAT APPLICATION SIMULATOR");
        System.out.println("====================================");
        seedDemoUsers();

        while (true) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> listUsers();
                case 3 -> sendMessage();
                case 4 -> processIncomingMessages();
                case 5 -> viewChatHistory();
                case 6 -> undoLastMessage();
                case 7 -> deleteLastMessage();
                case 8 -> {
                    System.out.println("Exiting Chat Application. Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please select 1-8.");
            }
        }
    }

    private static void seedDemoUsers() {
        users.put("krish", new User("krish"));
        users.put("riya", new User("riya"));
        users.put("aman", new User("aman"));
        System.out.println("Demo users created: krish, riya, aman");
    }

    private static void printMenu() {
        System.out.println("\n------------- MAIN MENU -------------");
        System.out.println("1. Register a user");
        System.out.println("2. Show all users");
        System.out.println("3. Send a message");
        System.out.println("4. Process incoming message queue");
        System.out.println("5. View chat history");
        System.out.println("6. Undo my last sent message");
        System.out.println("7. Delete my last chat message");
        System.out.println("8. Exit");
    }

    private static void registerUser() {
        String name = readNonEmpty("Enter a username: ").toLowerCase(Locale.ROOT);
        if (users.containsKey(name)) {
            System.out.println("That username already exists.");
            return;
        }
        users.put(name, new User(name)); // Hashing: average O(1) lookup/insert
        System.out.println("User '" + name + "' registered successfully.");
    }

    private static void listUsers() {
        if (users.isEmpty()) {
            System.out.println("No users registered.");
            return;
        }
        System.out.println("Registered users:");
        for (String name : users.keySet()) {
            System.out.println("- " + name);
        }
    }

    private static void sendMessage() {
        String senderName = readNonEmpty("Sender username: ").toLowerCase(Locale.ROOT);
        String receiverName = readNonEmpty("Receiver username: ").toLowerCase(Locale.ROOT);

        User sender = users.get(senderName);       // HashMap lookup
        User receiver = users.get(receiverName);   // HashMap lookup

        if (sender == null || receiver == null) {
            System.out.println("Sender or receiver does not exist. Register first.");
            return;
        }
        if (senderName.equals(receiverName)) {
            System.out.println("You cannot send a message to yourself.");
            return;
        }

        String text = readNonEmpty("Message: ");
        Message message = new Message(senderName, receiverName, text);

        incomingQueue.offer(message); // Queue: enqueue incoming message
        sender.sentStack.push(message); // Stack: remember sent message for undo

        System.out.println("Message queued successfully.");
        System.out.println("Use option 4 to deliver queued messages.");
    }

    private static void processIncomingMessages() {
        if (incomingQueue.isEmpty()) {
            System.out.println("Incoming message queue is empty.");
            return;
        }

        System.out.println("Processing messages in FIFO order:");
        while (!incomingQueue.isEmpty()) {
            Message message = incomingQueue.poll(); // Queue: dequeue oldest message
            User receiver = users.get(message.receiver);
            User sender = users.get(message.sender);

            // Add the same message to both users' chat histories.
            receiver.history.add(message);
            sender.history.add(message);

            System.out.println("Delivered: " + message);
        }
    }

    private static void viewChatHistory() {
        String first = readNonEmpty("Enter your username: ").toLowerCase(Locale.ROOT);
        String second = readNonEmpty("Chat with (username): ").toLowerCase(Locale.ROOT);

        User user = users.get(first);
        User other = users.get(second);
        if (user == null || other == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("\nChat history: " + first + " <-> " + second);
        boolean found = false;
        for (Message message : user.history) {
            boolean belongsToChat =
                    (message.sender.equals(first) && message.receiver.equals(second)) ||
                    (message.sender.equals(second) && message.receiver.equals(first));
            if (belongsToChat) {
                System.out.println(message);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No delivered messages in this chat yet.");
        }
    }

    private static void undoLastMessage() {
        String name = readNonEmpty("Your username: ").toLowerCase(Locale.ROOT);
        User user = users.get(name);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if (user.sentStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }

        Message last = user.sentStack.pop(); // Stack: LIFO
        boolean removedFromQueue = incomingQueue.remove(last);
        boolean removedFromHistory = false;

        // If already delivered, remove it from both users' histories.
        if (!removedFromQueue) {
            User receiver = users.get(last.receiver);
            if (receiver != null) removedFromHistory |= receiver.history.remove(last);
            removedFromHistory |= user.history.remove(last);
        }

        if (removedFromQueue || removedFromHistory) {
            System.out.println("Undone: " + last);
        } else {
            System.out.println("Message was not found; nothing changed.");
        }
    }

    private static void deleteLastMessage() {
        String name = readNonEmpty("Your username: ").toLowerCase(Locale.ROOT);
        User user = users.get(name);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        // Delete the newest message involving this user from their history.
        Iterator<Message> iterator = user.history.descendingIterator();
        Message latest = null;
        while (iterator.hasNext()) {
            Message candidate = iterator.next();
            if (candidate.sender.equals(name) || candidate.receiver.equals(name)) {
                latest = candidate;
                break;
            }
        }

        if (latest == null) {
            System.out.println("No chat message to delete.");
            return;
        }

        user.history.remove(latest);
        User other = users.get(latest.sender.equals(name) ? latest.receiver : latest.sender);
        if (other != null) other.history.remove(latest);
        System.out.println("Deleted from both chat histories: " + latest);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }
}
