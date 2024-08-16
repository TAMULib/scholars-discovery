package edu.tamu.scholars.middleware.service;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.tamu.scholars.middleware.config.model.IndexConfig;

public class CachingSolrClient<C extends SolrClient> extends SolrClient {

    private static final Logger logger = LoggerFactory.getLogger(CachingSolrClient.class);

    private static final String BASE_PATH = "basePath";
    private static final String BASIC_AUTH_USER = "basicAuthUser";
    private static final String BASIC_AUTH_PASSWORD = "basicAuthPassword";
    private static final String COLLECTION = "collection";

    private static final String HEADERS = "headers";
    private static final String METHOD = "method";
    private static final String PARAMS = "params";
    private static final String PATH = "path";

    private static final String PREFERRED_NODES = "preferredNodes";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String REQUEST_TYPE = "requestType";

    private final Map<String, File> lookup;

    private final Map<String, Map<String, String>> map;

    // there are different type of SolrClient
    C client;

    JwtTokenService jwtTokenService;

    IndexConfig index;

    ObjectMapper objectMapper;

    public CachingSolrClient(C client, JwtTokenService jwtTokenService, IndexConfig index, ObjectMapper objectMapper) {
        this.lookup = new ConcurrentHashMap<>();
        this.map = new ConcurrentHashMap<>();
        this.client = client;
        this.jwtTokenService = jwtTokenService;
        this.index = index;
        this.objectMapper = objectMapper;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
            throws SolrServerException, IOException {

        if (!request.getMethod().equals(org.apache.solr.client.solrj.SolrRequest.METHOD.GET) || index.isSchematize()
                || index.isOnStartup() || Objects.isNull(request)) {
            return client.request(request, collection);
        }

        HttpServletRequest origatingRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String cachePath = origatingRequest.getRequestURI();

        if (
            cachePath.startsWith("/individual") &&
            !cachePath.startsWith("/individual/analytics") &&
            !cachePath.startsWith("/individual/search")
        ) {
            cachePath = "/individual";
        }

        String key = String.format("src/test/resources%s/lookup_table", cachePath);

        File lookupFile = this.lookup.get(key);

        if (lookupFile == null) {
            lookupFile = new File(key + ".sd");
            this.lookup.put(key, lookupFile);
        }

        Map<String, String> innerMap = this.map.get(key);

        if (innerMap == null) {
            innerMap = lookupFile.exists() && !lookupFile.isDirectory()
                    ? objectMapper.readValue(lookupFile, new TypeReference<ConcurrentHashMap<String, String>>() { })
                    : new ConcurrentHashMap<>();
            this.map.put(key, innerMap);
        }

        long start = System.currentTimeMillis();

        ObjectNode rootNode = objectMapper.createObjectNode();

        rootNode.put(BASE_PATH, request.getBasePath());
        rootNode.put(BASIC_AUTH_USER, request.getBasicAuthUser());
        rootNode.put(BASIC_AUTH_PASSWORD, request.getBasicAuthPassword());
        rootNode.put(COLLECTION, request.getCollection());

        if (Objects.nonNull(request.getHeaders())) {
            ObjectNode headers = objectMapper.createObjectNode();
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                headers.put(header.getKey(), header.getValue());
            }
            rootNode.set(HEADERS, headers);
        }

        rootNode.put(METHOD, request.getMethod().toString());

        if (Objects.nonNull(request.getParams()) && Objects.nonNull(request.getParams().jsonStr())) {
            ObjectNode params = (ObjectNode) objectMapper.readTree(request.getParams().jsonStr());
            rootNode.set(PARAMS, params);
        }

        rootNode.put(PATH, request.getPath());

        if (Objects.nonNull(request.getPreferredNodes())) {
            ArrayNode preferredNodes = objectMapper.createArrayNode();
            for (String preferredNode : request.getPreferredNodes()) {
                preferredNodes.add(preferredNode);
            }
            rootNode.set(PREFERRED_NODES, preferredNodes);
        }

        if (Objects.nonNull(request.getQueryParams())) {
            ArrayNode queryParams = objectMapper.createArrayNode();
            for (String queryParam : request.getQueryParams()) {
                queryParams.add(queryParam);
            }
            rootNode.set(QUERY_PARAMS, queryParams);
        }

        rootNode.put(REQUEST_TYPE, request.getRequestType());

        // System.out.println("getResponseParser: " + request.getResponseParser());
        // System.out.println("getUserPrincipal: " + request.getUserPrincipal());

        logger.info("{}: {} seconds", "DESERIALIZE", (System.currentTimeMillis() - start) / (double) 1000);

        long startClaimsToJWTUUID = System.currentTimeMillis();

        Map<String, Object> claims = objectMapper.readValue(rootNode.toPrettyString(), new TypeReference<Map<String, Object>>() { });

        String jwt = jwtTokenService.createToken(collection, claims);

        byte[] bytes = jwt.getBytes(StandardCharsets.UTF_8);
        String uuid = UUID.nameUUIDFromBytes(bytes).toString();

        logger.info("{}: {} seconds", "CLAIMS_TO_JWT_UUID", (System.currentTimeMillis() - startClaimsToJWTUUID) / (double) 1000);

        NamedList<Object> response = null;

        File directory = new File(key);

        directory.mkdirs();

        String filename = String.format("%s/%s", key, uuid);

        File file = new File(filename);

        long startFoundCache = System.currentTimeMillis();
        long startQueryToSolr = System.currentTimeMillis();

        if (file.exists()) {
            startFoundCache = System.currentTimeMillis();

            try (
                FileInputStream fileIn = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(fileIn);
            ) {
                response = (NamedList<Object>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            logger.info("{}:{}: {} seconds", uuid, "RESPONSE CACHED READ", (System.currentTimeMillis() - startFoundCache) / (double) 1000);
        } else {
            startQueryToSolr = System.currentTimeMillis();
            response = client.request(request, collection);

            try (
                FileOutputStream fileOut = new FileOutputStream(filename);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
            ) {
                out.writeObject(response);
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("{}:{}: {} seconds", uuid, "QUERY RESPONSE", (System.currentTimeMillis() - startQueryToSolr) / (double) 1000);

            long startUdateLookupTable = System.currentTimeMillis();

            innerMap.put(jwt, uuid);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(lookupFile, innerMap);

            logger.info("{}:{}: {} seconds", uuid, "UPDATE LOOKUP TABLE", (System.currentTimeMillis() - startUdateLookupTable) / (double) 1000);

            logger.info("{}:{}:{} {}", uuid, "REQUEST", uuid, rootNode.toPrettyString());
        }
        logger.info("{}:{}: {} seconds", uuid, "ACTUAL RESPONSE", (System.currentTimeMillis() - start) / (double) 1000);

        return response;
    }

}
