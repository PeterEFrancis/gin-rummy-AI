import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        ArrayList<Integer> A = new ArrayList<>();
        A.add(1);
        ArrayList<Integer> B = A;
        A = new ArrayList<Integer>();
        System.out.println(B.size());
    }
}
