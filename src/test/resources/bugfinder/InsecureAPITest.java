import java.security.MessageDigest
import java.security.NoSuchAlgorithmException;

class InsecureAPITest{
    public static void main(String[] args) throws NoSuchAlgorithmException {
        InsecureAPITest un = new InsecureAPITest();
        un.unsafe();

        A a = new A();
        a.unsafe1();

        MessageDigest md1 = MessageDigest.getInstance("md5");
        MessageDigest md2 = MessageDigest.getInstance("SHA1");
        MessageDigest md3 = MessageDigest.getInstance("SHA-256");
    }

    public void unsafe(){

    }
}

class A{
    public void unsafe1(){
        int a = 3;
        new B().unsafe2(3, "MD5");
        new B().unsafe2(3, "");
        new B().unsafe2(3, null);
        new B().unsafe2(a, null);
    }

    protected void safe(){

    }
}

class B{
    public int unsafe2(int i, String str){
        return 0;
    }
}
