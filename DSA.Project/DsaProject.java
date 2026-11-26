import java.util.*;

public class DsaProject {

    static class Customer {
        int id;
        int at;
        int st;
        int priority;
        int rem;
        int ct;
        int wt;
        int tat;
        boolean arrivedInQueue;

        Customer(int id, int at, int st, int priority) {
            this.id = id;
            this.at = at;
            this.st = st;
            this.priority = priority;
            this.rem = st;
        }
    }

    static class Node {
        Customer data;
        Node next;

        Node(Customer data) {
            this.data = data;
        }
    }

    static class CustomerList {
        Node head;

        void add(Customer c) {
            Node n = new Node(c);
            if (head == null) {
                head = n;
                return;
            }
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = n;
        }

        void sortByArrival() {
            head = mergeSort(head);
        }

        private Node mergeSort(Node h) {
            if (h == null || h.next == null) return h;
            Node mid = getMid(h);
            Node right = mid.next;
            mid.next = null;
            return merge(mergeSort(h), mergeSort(right));
        }

        private Node getMid(Node h) {
            Node slow = h, fast = h.next;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }
            return slow;
        }

        private Node merge(Node a, Node b) {
            Node dummy = new Node(null), tail = dummy;
            while (a != null && b != null) {
                if (a.data.at < b.data.at || (a.data.at == b.data.at && a.data.id <= b.data.id)) {
                    tail.next = a;
                    a = a.next;
                } else {
                    tail.next = b;
                    b = b.next;
                }
                tail = tail.next;
            }
            tail.next = (a != null) ? a : b;
            return dummy.next;
        }

        List<Customer> toList() {
            List<Customer> list = new ArrayList<>();
            for (Node cur = head; cur != null; cur = cur.next) {
                Customer c = cur.data;
                Customer copy = new Customer(c.id, c.at, c.st, c.priority);
                list.add(copy);
            }
            return list;
        }
    }

    static class CircularQueue {
        private final Customer[] arr;
        private int front = 0, rear = 0, size = 0;

        CircularQueue(int capacity) {
            arr = new Customer[Math.max(1, capacity)];
        }

        boolean isEmpty() {
            return size == 0;
        }

        void enqueue(Customer c) {
            if (size == arr.length) return;
            arr[rear] = c;
            rear = (rear + 1) % arr.length;
            size++;
        }

        Customer dequeue() {
            if (isEmpty()) return null;
            Customer c = arr[front];
            arr[front] = null;
            front = (front + 1) % arr.length;
            size--;
            return c;
        }
    }

    static class Segment {
        int customerId, start, end;

        Segment(int customerId, int start, int end) {
            this.customerId = customerId;
            this.start = start;
            this.end = end;
        }
    }

    static void runPriority(List<Customer> customers) {
        PriorityQueue<Customer> pq = new PriorityQueue<>((a, b) -> {
            if (a.priority != b.priority) return Integer.compare(a.priority, b.priority);
            if (a.at != b.at) return Integer.compare(a.at, b.at);
            return Integer.compare(a.id, b.id);
        });

        List<Segment> timeline = new ArrayList<>();
        int idx = 0, time = 0, done = 0, n = customers.size();

        while (done < n) {
            while (idx < n && customers.get(idx).at <= time) {
                pq.offer(customers.get(idx++));
            }
            if (pq.isEmpty()) {
                time = Math.max(time, customers.get(idx).at);
                continue;
            }
            Customer cur = pq.poll();
            int start = time;
            int end = start + cur.st;
            timeline.add(new Segment(cur.id, start, end));
            time = end;
            cur.ct = end;
            cur.tat = cur.ct - cur.at;
            cur.wt = cur.tat - cur.st;
            done++;
        }

        printReport("Non-Preemptive Priority Service", customers, timeline);
    }

    static void runRoundRobin(List<Customer> customers, int quantum) {
        int n = customers.size();
        CircularQueue cq = new CircularQueue(n + 2);
        List<Segment> timeline = new ArrayList<>();
        int idx = 0, time = 0, done = 0;

        while (done < n) {
            while (idx < n && customers.get(idx).at <= time) {
                Customer c = customers.get(idx++);
                if (!c.arrivedInQueue) {
                    c.arrivedInQueue = true;
                    cq.enqueue(c);
                }
            }

            if (cq.isEmpty()) {
                time = Math.max(time, customers.get(idx).at);
                continue;
            }

            Customer cur = cq.dequeue();
            int slice = Math.min(quantum, cur.rem);
            int start = time;
            int end = start + slice;
            timeline.add(new Segment(cur.id, start, end));
            time = end;
            cur.rem -= slice;

            // Add new arrivals that arrived up to current time before re-queuing cur
            while (idx < n && customers.get(idx).at <= time) {
                Customer c = customers.get(idx++);
                if (!c.arrivedInQueue) {
                    c.arrivedInQueue = true;
                    cq.enqueue(c);
                }
            }

            if (cur.rem > 0) {
                cq.enqueue(cur);
            } else {
                cur.ct = end;
                cur.tat = cur.ct - cur.at;
                cur.wt = cur.tat - cur.st;
                done++;
            }
        }

        printReport("Round Robin Service (Quantum = " + quantum + " ms)", customers, timeline);
    }

    static void printReport(String title, List<Customer> customers, List<Segment> timeline) {
        System.out.println("\n=== " + title + " ===");
        System.out.print("Timeline: ");
        for (int i = 0; i < timeline.size(); i++) {
            Segment s = timeline.get(i);
            System.out.print("C" + s.customerId + " (" + s.start + "-" + s.end + ")");
            if (i < timeline.size() - 1) System.out.print(" | ");
        }
        System.out.println();

        double sumWt = 0, sumTat = 0;
        for (Customer c : customers) {
            sumWt += c.wt;
            sumTat += c.tat;
            System.out.printf("C%d -> AT=%d, ST=%d, P=%d, CT=%d, WT=%d ms, TAT=%d ms%n",
                    c.id, c.at, c.st, c.priority, c.ct, c.wt, c.tat);
        }
        System.out.printf("Average WT  = %.2f ms%n", sumWt / customers.size());
        System.out.printf("Average TAT = %.2f ms%n", sumTat / customers.size());
    }

    static int readPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                int val = sc.nextInt();
                if (val > 0) return val;
            } else {
                sc.next();
            }
            System.out.println("Invalid input! Please enter a positive integer (> 0).");
        }
    }

    static int readNonNegativeInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                int val = sc.nextInt();
                if (val >= 0) return val;
            } else {
                sc.next();
            }
            System.out.println("Invalid input! Please enter a non-negative integer (>= 0).");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int algo;
        while (true) {
            System.out.println("Select Algorithm: 1 = Non-Preemptive Priority, 2 = Round Robin");
            System.out.print("Choice: ");
            if (sc.hasNextInt()) {
                algo = sc.nextInt();
                if (algo == 1 || algo == 2) break;
            } else {
                sc.next();
            }
            System.out.println("Invalid choice! Enter 1 or 2.");
        }

        int n = readPositiveInt(sc, "Enter number of customers: ");
        CustomerList list = new CustomerList();

        for (int i = 1; i <= n; i++) {
            System.out.println("--- Customer C" + i + " ---");
            int at = readNonNegativeInt(sc, "Arrival Time (ms, >= 0): ");
            int st = readPositiveInt(sc, "Service Time (ms, > 0): ");
            int prio = readPositiveInt(sc, "Priority (> 0, smaller = higher): ");
            list.add(new Customer(i, at, st, prio));
        }

        int quantum = 1;
        if (algo == 2) {
            quantum = readPositiveInt(sc, "Enter Time Quantum (ms, > 0): ");
        }

        list.sortByArrival();
        List<Customer> sorted = list.toList();

        if (algo == 1) {
            runPriority(sorted);
        } else {
            runRoundRobin(sorted, quantum);
        }

        sc.close();
    }
}