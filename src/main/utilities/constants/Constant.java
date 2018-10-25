package main.utilities.constants;

public final class Constant {
    private Constant() {}  // Restrict initiation
    
    public static final String WHITESPACE = " ";
    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String END_OF_STREAM = "EOS";
    public static final String HEARTBEAT = "Heatbeat";
    
    public static final int CHUNK_SIZE = 1024;
    public static final String CHUNK_EXT = ".chunk";
}