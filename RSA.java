import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger z;
    private BigInteger e;
    private BigInteger d;
    private SecureRandom sr;
    private int bitLen;

    public RSA (int bitLen){
        sr = new SecureRandom();
        this.bitLen = bitLen;
        p = BigInteger.probablePrime(this.bitLen, sr);
        q = BigInteger.probablePrime(this.bitLen, sr);
        n = p.multiply(q);
        z = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Select a value for e that is less than z, and has a gcd of one with z
        e = BigInteger.probablePrime(this.bitLen - 2, sr);
        while (z.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(z) < 0)
        {
            e.add(BigInteger.ONE);
        }

        d = e.modInverse(z);
    }

    //Used for the client to encrypt traffic with public key
    public RSA (BigInteger e, BigInteger n)
    {
        this.e = e;
        this.n = n;
    }

    public byte[] encrypt(byte[] plainText)
    {
        return (new BigInteger(plainText)).modPow(e, n).toByteArray();
    }

    public byte[] decrypt(byte[] cipherText)
    {
        return (new BigInteger(cipherText)).modPow(d, n).toByteArray();
    }

    public byte[] getPublicKey()
    {
        byte[] e = this.e.toByteArray();
        byte[] n = this.n.toByteArray();

        byte[] publicKey = Utilities.combineArrays(e, n);

        return publicKey;
    }

    public int getELen()
    {
        return this.e.toByteArray().length;
    }

    public int getNLen()
    {
        return this.n.toByteArray().length;
    }

    public BigInteger getE()
    {
        return this.e;
    }

    public BigInteger getN()
    {
        return this.n;
    }

}