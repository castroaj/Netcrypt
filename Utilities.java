public class Utilities {
    public static byte[] combineArrays(byte[] array1, byte[] array2) {

        byte[] newArray = new byte[array1.length + array2.length];
        
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);

        return newArray;
    }
}