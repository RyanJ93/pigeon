package pigeonServer.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class StorageService extends Service {
    public static final String STORAGE_DIRECTORY_PATH = "storage/";
    public static final int ORDER_BY_DESC = -1;
    public static final int ORDER_BY_ASC = 1;

    private static String generateKeyHash(String key){
        try{
            String digest = "";
            if ( key != null && !key.isEmpty() ){
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(StandardCharsets.UTF_8.encode(key));
                digest = String.format("%032x", new BigInteger(1, messageDigest.digest()));
            }
            return digest;
        }catch(NoSuchAlgorithmException ex){
            throw new RuntimeException("Cannot generate a hash for the given key");
        }
    }

    private static HashSet<String> generateHashedKeySet(String[] keys){
        HashSet<String> hashedKeys = new HashSet<>();
        for ( String key : keys ){
            hashedKeys.add(StorageService.generateKeyHash(key));
        }
        return hashedKeys;
    }

    private static String serializeDocument(HashMap<String, String> document){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.toJson(document);
    }

    private static HashMap<String, String> extractDocumentFromFile(String path) throws IOException {
        BufferedReader bufferedReader = null;
        try{
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            while ( ( line = bufferedReader.readLine() ) != null ){
                stringBuilder.append(line);
            }
            HashMap<String, String> document = new HashMap<>();
            Gson gson = new Gson();
            JsonObject documentProperties = gson.fromJson(stringBuilder.toString(), JsonObject.class);
            Set<Map.Entry<String, JsonElement>> properties = documentProperties.entrySet();
            for ( Map.Entry<String, JsonElement> property : properties ){
                document.put(property.getKey(), property.getValue().getAsString());
            }
            return document;
        }finally{
            if ( bufferedReader != null ){
                bufferedReader.close();
            }
        }
    }

    private static HashMap<String, String> mergeDocuments(HashMap<String, String> found, HashMap<String, String> given){
        HashMap<String, String> mergedDocument = new HashMap<>();
        for ( Map.Entry<String, String> property : found.entrySet() ){
            mergedDocument.put(property.getKey(), property.getValue());
        }
        for ( Map.Entry<String, String> property : given.entrySet() ){
            mergedDocument.put(property.getKey(), property.getValue());
        }
        return mergedDocument;
    }

    private static void ensureDirectory(String directory){
        File storageDirectory = new File(directory);
        if ( !storageDirectory.exists() ){
            storageDirectory.mkdirs();
        }
    }

    private String namespace = "global";
    private String entity = null;

    private String getBasePath() throws IOException {
        if ( this.entity == null || this.entity.isEmpty() ){
            throw new IOException("No entity defined.");
        }
        String path = StorageService.STORAGE_DIRECTORY_PATH + "/" + this.namespace + "/" + this.entity;
        StorageService.ensureDirectory(path);
        return path;
    }

    public StorageService setNamespace(String namespace){
        this.namespace = namespace == null || namespace.isEmpty() ? "global" : namespace;
        return this;
    }

    public String getNamespace(){
        return this.namespace;
    }

    public StorageService setEntity(String entity){
        this.entity = entity;
        return this;
    }

    public String getEntity(){
        return this.entity;
    }

    public ArrayList<HashMap<String, String>> find(String[] keys, String orderByField, int orderByDirection) throws IOException {
        ArrayList<HashMap<String, String>> documents = new ArrayList<>();
        String basePath = this.getBasePath();
        if ( keys == null ){
            File storageDirectory = new File(basePath);
            String[] documentFiles = storageDirectory.list();
            if ( documentFiles != null ){
                for ( String documentFile : documentFiles ){
                    int index = documentFile.lastIndexOf(".");
                    if ( index > 0 && documentFile.substring(index + 1).equalsIgnoreCase("json") ){
                        documents.add(StorageService.extractDocumentFromFile(basePath + "/" + documentFile));
                    }
                }
            }
        }else{
            HashSet<String> hashedKeys = StorageService.generateHashedKeySet(keys);
            for ( String hashedKey : hashedKeys ){
                String path = basePath + "/" + hashedKey + ".json";
                File documentFile = new File(path);
                if ( documentFile.exists() && documentFile.isFile() ){
                    documents.add(StorageService.extractDocumentFromFile(path));
                }
            }
        }
        if ( orderByField != null && !orderByField.isEmpty() ){
            documents.sort((a, b) -> {
                int relation = a.get(orderByField).compareTo(b.get(orderByField));
                return orderByDirection == StorageService.ORDER_BY_DESC ? -relation : relation;
            });
        }
        return documents;
    }

    public HashMap<String, String> findOne(String key) throws IOException {
        ArrayList<HashMap<String, String>> results = this.find(new String[]{key}, null, 0);
        return results.size() == 0 ? null : results.get(0);
    }

    public boolean exists(String key) throws IOException {
        File documentFile = new File(this.getBasePath() + "/" + StorageService.generateKeyHash(key) + ".json");
        return documentFile.exists() && documentFile.isFile();
    }

    public int deleteMany(String[] keys) throws IOException {
        HashSet<String> hashedKeys = StorageService.generateHashedKeySet(keys);
        String basePath = this.getBasePath();
        int counter = 0;
        for ( String hashedKey : hashedKeys ){
            File documentFile = new File(basePath + "/" + hashedKey + ".json");
            if ( documentFile.exists() && documentFile.isFile() && documentFile.delete() ){
                counter++;
            }
        }
        return counter;
    }

    public int deleteOne(String key) throws IOException{
        return this.deleteMany(new String[]{key});
    }

    public int insert(String key, HashMap<String, String> data) throws IOException {
        if ( this.exists(key) ){
            throw new IllegalArgumentException("Duplicate key found.");
        }
        FileWriter fileWriter = null;
        int counter = 0;
        try{
            fileWriter = new FileWriter(this.getBasePath() + "/" + StorageService.generateKeyHash(key) + ".json");
            fileWriter.write(StorageService.serializeDocument(data));
            counter++;
        }finally{
            if ( fileWriter != null ){
                fileWriter.close();
            }
        }
        return counter;
    }

    public int update(String key, HashMap<String, String> data, boolean replace) throws IOException {
        String path = this.getBasePath() + "/" + StorageService.generateKeyHash(key) + ".json";
        File documentFile = new File(path);
        FileWriter fileWriter = null;
        int counter = 0;
        if ( documentFile.exists() && documentFile.isFile() ){
            if ( !replace ){
                HashMap<String, String> document = StorageService.extractDocumentFromFile(path);
                data = StorageService.mergeDocuments(document, data);
            }
            try{
                fileWriter = new FileWriter(documentFile);
                fileWriter.write(StorageService.serializeDocument(data));
                counter++;
            }finally{
                if ( fileWriter != null ){
                    fileWriter.close();
                }
            }
        }
        return counter;
    }
}
