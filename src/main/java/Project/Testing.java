package Project;

public class Testing {
    private int a;
    private Testing(int _a){
        a = _a;
    }
    private Testing changing(){
        return new Testing(100);
    }

    public static void main(String[] args){
        Testing foo = new Testing(10);
        System.out.println(foo);
        System.out.println(foo.changing());

    }
}
