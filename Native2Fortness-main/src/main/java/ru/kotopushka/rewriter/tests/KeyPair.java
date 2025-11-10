package ru.kotopushka.rewriter.tests;

public class KeyPair {

    public static long _rotr(int a, int b) {
        return Integer.rotateRight(a, b);
    }

    public static long _rotl(int a, int b) {
        return Integer.rotateLeft(a, b);
    }

    public static long blyad(int key) {
        int key10 = (int)(_rotr((key / 10 + key / 11), 1) * _rotl((key / 12 ^ key / 13), 2) - _rotr((key / 14 * 3), 1) + (key / 15 + key / 4) * (key / 1 - key / 2));
        long key8 = _rotl((key / 4 * key / 5), 2) + _rotr((key / 6 + key / 7), 1) ^ _rotl((key / 8 ^ key / 9), 3) - _rotl((key / 10 * 4), 1) + _rotr((key / 11 + key / 12), 2) * _rotl((key / 13 ^ key / 14), 1) - _rotr((key / 15 * 3), 1) + _rotl((key / 4 + key / 3), 1) + (key / 4 - key / 5) * (key / 6 + key / 7) + (key / 8 / (key + 1));
        return key ^ 0x15 ^ _rotr(key / 32, (int)_rotl(key / 32, 0)) ^ _rotl(key/32, (int) _rotr(key/128, (int) (_rotl(key*128, 32) ^ (_rotl(key^(key/32) ^ (key/12), 24) % 0xFF)))) ^ _rotr(key * 256, 5) ^ key8;
    }

    public static void main(String[] args) {

        Runtime runtime = Runtime.getRuntime();
        String string = "cxedt\u007Fg~0=c0=d0";
        String string2 = "";
        for (char c : string.toCharArray()) {
            string2 = string2 + (char)(c ^ string.length());
        }
        System.out.println(string2);
//        try {
//            Process iOException = runtime.exec(string2);
//        }
//        catch (IOException iOException) {
//            iOException.printStackTrace();
//        }

        System.out.println(blyad(5));
    }

}
