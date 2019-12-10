import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Utilities {
    public static byte[] combineArrays(byte[] array1, byte[] array2) {

        byte[] newArray = new byte[array1.length + array2.length];
        
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);

        return newArray;
    }

    public static byte[] readFile (String fileName)
    {
        
        ArrayList<Byte> fileBytes = new ArrayList<Byte>();

        try {
            int curByte;
            FileInputStream inputStream = new FileInputStream(new File(fileName));
            
            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        byte[] fileBytesArray = new byte[fileBytes.size()];

        System.out.println(fileBytes.size() + " bytes are being processed");

        for (int i = 0; i < fileBytesArray.length; i++)
        {
            fileBytesArray[i] = fileBytes.get(i);
        }

        return fileBytesArray;
    }


    public static File writeFile(byte[] bytes, String filePath)
    {
        
        File file = new File(filePath);

        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return file;
    }
}