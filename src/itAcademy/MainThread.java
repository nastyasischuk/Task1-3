package itAcademy;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class MainThread {
    static ThreadFinder threadFinder = new ThreadFinder();

    public static final Sequence POISON = new Sequence().setLength(-1);

    public void start() throws Exception {

        BlockingQueue<Sequence> blockingQueue = threadFinder.getBlockingQueue();
        while (true) {
            threadFinder.getFileNameExchanger().exchange(getFileName());
            while (true) {
                Sequence takenSequence = blockingQueue.take();

                if (takenSequence.getLength() == POISON.getLength()) {
                    break;
                }
                System.out.println(takenSequence);
            }

            Sequence sequence = threadFinder.getSendingResultSequence().exchange(null);
            System.out.println("Result sequence");
            System.out.println(sequence);
        }
    }


    public String getFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("File name ");
        String filename = scanner.nextLine();
        return filename;
    }

}
