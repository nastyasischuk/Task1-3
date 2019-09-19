package itAcademy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadFinder implements Runnable {
    private Exchanger<String> fileNameGetter;
    private Exchanger<Sequence> sendingResultSequence;
    private BlockingQueue<Sequence> sendingRepeatedSequences;

    private FinderSequence finderSequence;
    byte[] fileBytes;
    private Thread thread;
    public static final Sequence POISON = new Sequence().setLength(-1);

    public ThreadFinder() {
        fileNameGetter = new Exchanger<>();
        sendingRepeatedSequences = new LinkedBlockingQueue<>();
        sendingResultSequence = new Exchanger<>();
        finderSequence = new FinderSequence();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                String filename = fileNameGetter.exchange(null);
                readFile(filename);
                finderSequence.findRepeatedSequences();
                sendFinalResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void readFile(String filename) {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(new File(filename)))) {
            fileBytes = new byte[reader.available()];
            reader.read(fileBytes, 0, fileBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFinalResult() {
        try {
            sendingRepeatedSequences.put(POISON);
            sendingResultSequence.exchange(finderSequence.getResultSequence());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Exchanger<String> getFileNameExchanger() {
        return fileNameGetter;
    }

    public Exchanger<Sequence> getSendingResultSequence() {
        return sendingResultSequence;
    }

    public BlockingQueue<Sequence> getBlockingQueue() {
        return sendingRepeatedSequences;
    }


    public class FinderSequence {

        private Sequence fetchSequenceByComparingParts(byte[] firstPart, byte[] secondPart) {
            int n = Math.min(firstPart.length, secondPart.length);
            int i;
            for (i = 0; i < n; i++) {
                if (firstPart[i] != secondPart[i]) {
                    if (i == 0)
                        return null;
                    break;
                }
            }

            return new Sequence(fileBytes.length - firstPart.length, fileBytes.length - secondPart.length, i);
        }

        public Sequence getResultSequence() throws InterruptedException {
            List<Sequence> allSequences = findRepeatedSequences();
            Collections.sort(allSequences, new Comparator<Sequence>() {
                @Override
                public int compare(Sequence o1, Sequence o2) {
                    return o2.getLength() - o1.getLength();
                }
            });
            return allSequences.get(0);
        }

        private List<Sequence> findRepeatedSequences() throws InterruptedException {
            byte[][] parts = breakIntoParts();

            List<Sequence> repeatedSequences = new ArrayList<>();
            for (int i = 0; i < fileBytes.length - 1; i++) {
                Sequence sequence = fetchSequenceByComparingParts(parts[i], parts[i + 1]);
                if (sequence != null) {
                    sendToMainThreadIntermediateSequences(sequence);
                    repeatedSequences.add(sequence);
                }
            }
            return repeatedSequences;
        }

        private void sendToMainThreadIntermediateSequences(Sequence sequence) {
            try {
                sendingRepeatedSequences.put(sequence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private byte[][] breakIntoParts() {
            byte[][] parts = new byte[fileBytes.length][];
            for (int i = 0; i < fileBytes.length; i++) {
                parts[i] = Arrays.copyOfRange(fileBytes, i, fileBytes.length);
            }
            Arrays.sort(parts, (o1, o2) -> {
                int i = -1;
                do {
                    i++;
                    if (i >= o1.length || i >= o2.length)
                        return o2.length - o1.length;
                } while (o1[i] == o2[i]);
                return o1[i] - o2[i];
            });

            return parts;
        }
    }
}
