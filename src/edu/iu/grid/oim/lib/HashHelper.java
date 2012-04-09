package edu.iu.grid.oim.lib;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashHelper {

	/*
    public static String calculateHash(MessageDigest algorithm, String fileName) throws Exception{

        FileInputStream     fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DigestInputStream   dis = new DigestInputStream(bis, algorithm);

        // read the file and update the hash calculation
        while (dis.read() != -1);

        // get the hash value as byte array
        byte[] hash = algorithm.digest();

        return byteArray2Hex(hash);
    }*/

    public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        //not sure what encoding is used in input..
        byte[] ret = sha1.digest(input.getBytes());
        return byteArray2Hex(ret);
    }
    
    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /*
    public static void main(String[] args) throws Exception {
        String fileName = "javablogging.png";

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5  = MessageDigest.getInstance("MD5");        

        System.out.println(calculateHash(sha1, fileName));
        System.out.println(calculateHash(md5, fileName));
    }
    */
}