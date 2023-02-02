import java.security.SecureRandom;
import java.util.Random;

public class RandomTest
{
    long randomTest()
    {
        Random r1 = new Random();
        Random r2 = new Random(200000);
        return r1.nextLong() + r2.nextLong();
    }

    double mathTest()
    {
        return Math.random();
    }

    long JVMRandomTest()
    {
        JVMRandom r = new JVMRandom();
        long r1 = r.nextLong();
        return JVMRandom.nextLong(r1);
    }

    double RandomUtilsTest()
    {
        RandomUtils r = new RandomUtils();
        Random rr = new Random();
        int r1 = RandomUtils.nextInt();
        int r2 = RandomUtils.nextInt(r1);
        int r3 = RandomUtils.nextInt(rr);
        int r4 = RandomUtils.nextInt(rr, r2);
        long r5 = r3 + r4;

        long r6 = RandomUtils.nextLong();
        long r7 = RandomUtils.nextLong(rr);
        r5 = r5 + r6 + r7;

        float r8 = RandomUtils.nextFloat();
        float r9 = RandomUtils.nextFloat(rr);

        double r10 = RandomUtils.nextDouble();
        double r11 = RandomUtils.nextDouble(rr);

        boolean r12 = RandomUtils.nextBoolean();
        boolean r13 = RandomUtils.nextBoolean(rr);

        double r14 = r8 + r9;
        if(r12 || r13)
        {
            r14 = r14 + r10 + r11;
        }
        return r14 + r5;
    }

    String RandomStringUtilsTest()
    {
        Random rr = new Random();
        RandomStringUtils r = new RandomStringUtils();
        int temp = 999;
        char[] tempStr = {'a','b','c'};
        String str1 = RandomStringUtils.random(temp);
        String str2 = RandomStringUtils.random(temp,str1);
        String str3 = RandomStringUtils.random(temp, tempStr);
        String str4 = RandomStringUtils.random(temp,false, true);
        String str5 = RandomStringUtils.random(temp,0,1,false,true);
        String str6 = RandomStringUtils.random(temp,0,1,false,true,tempStr);
        String str7 = RandomStringUtils.random(temp,0,1,false,true,tempStr,rr);
        String str9 = RandomStringUtils.randomAscii(temp);
        //String str10 = RandomStringUtils.randomAscii(temp,temp);
        String str11 = RandomStringUtils.randomNumeric(temp);
        //String str12= RandomStringUtils.randomNumeric(temp,temp);
        String str13 = RandomStringUtils.randomAlphabetic(temp);
        //String str14 = RandomStringUtils.randomAlphabetic(temp,temp);
        String str15 = RandomStringUtils.randomAlphanumeric(temp);
        //String str16 = RandomStringUtils.randomAlphanumeric(temp,temp);
        //String str17 = RandomStringUtils.randomGraph(temp);
        //String str18 = RandomStringUtils.randomGraph(temp,temp);
        //String str19 = RandomStringUtils.randomPrint(temp);
        //String str20 = RandomStringUtils.randomPrint(temp,temp);
        return str1 + str2 + str3 + str4 + str5 + str6 + str7 + str9 + str11 + str13 + str15;
    }

    long securityRandom()
    {
        SecureRandom r = new SecureRandom();
        return r.nextLong();
    }
};
