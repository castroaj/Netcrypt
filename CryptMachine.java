import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class CryptMachine {

    private File inputFile;
    private File encryptedFile;
    private String encryptedFilePath;
   
    private FileInputStream inputStream;
    private BufferedWriter fileWriter;

    private ArrayList<Byte> fileBytes;

    public File encryptFile(String filePath)
    {
        inputFile = new File(filePath);
        encryptedFilePath = "NetCry-" + filePath;
        encryptedFile = new File(encryptedFilePath);
        fileBytes = new ArrayList<Byte>();

        System.out.println("NETCRYPT will now encrypt" + filePath + "\n\n");

        try 
        {
            fileWriter = new BufferedWriter(new FileWriter(encryptedFile));
            inputStream = new FileInputStream(inputFile);

            int curByte;

            while ((curByte = inputStream.read()) != -1)
            {
                fileBytes.add((byte) curByte);
            }
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encryptedFile;
    } 

}