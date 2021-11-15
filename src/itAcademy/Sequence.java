package itAcademy;

class Sequence {
    private int first;
    private int second;
    private int length;

    public Sequence() {
    }

    public Sequence(int first, int second, int length) {
        this.first = first;
        this.second = second;
        this.length = length;
    }

    public int getLength() {
        return length;

    }

    public Sequence setLength(int length) {
        this.length = length;
        return this;
    }

    @Override
    public String toString() {
        return "Sequence{" +
                "first=" + first +
                ", second=" + second +
                ", length=" + length +
                '}';
    }

}