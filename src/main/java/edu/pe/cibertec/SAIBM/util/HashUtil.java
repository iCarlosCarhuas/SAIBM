package edu.pe.cibertec.SAIBM.util;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class HashUtil {

    public static String Nuevo(String texto) {
        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putString(texto, Charsets.UTF_8);
        HashCode sha256 = hasher.hash();

        return sha256.toString();
    }

}
