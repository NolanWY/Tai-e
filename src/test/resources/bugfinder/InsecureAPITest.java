import java.io.IOException;
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException;

class UnsafeAPITest{
    public static void main(String[] args) throws NoSuchAlgorithmException {
        UnsafeAPITest un = new UnsafeAPITest();
        un.unsafe(1);

        A a = new A();
        a.unsafe1();

        MessageDigest md1 = MessageDigest.getInstance("md5");
        MessageDigest md2 = MessageDigest.getInstance("SHA1");
        MessageDigest md3 = MessageDigest.getInstance("SHA-256");
    }

    public void unsafe(int i){

    }
}

class A{
    public void unsafe1(){
        new B().unsafe2(3);
    }

    protected void safe(){

    }
}

class B{
    public int unsafe2(int i){
        return 0;
    }
}
