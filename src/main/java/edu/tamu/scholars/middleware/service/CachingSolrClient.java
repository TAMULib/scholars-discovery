package edu.tamu.scholars.middleware.service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.service.builder.ClaimBuilder;
import edu.tamu.scholars.middleware.utility.JavaObjectStorageFileUtility;

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

    private static final String LEFT_CURLY_BRACKET = "{";
    private static final String RIGHT_CURLY_BRACKET = "}";

    private static final String FORWARD_SLASH = "/";

    // < key, File >
    private final Map<String, File> lookup;

    // < key, < JWT, UUID > >
    private final Map<String, Map<String, String>> map;

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

    @PostConstruct
    public void clearCache() {
        if (index.isCacheEnabled()) {
            File cacheDirectory = new File(StringUtils.removeEnd(index.getCacheLocation(), FORWARD_SLASH));
            if (cacheDirectory.exists() && !cacheDirectory.isDirectory()) {
                throw new RuntimeException(String.format("Cache location %s is not a directory!", index.getCacheLocation()));
            }
            if (index.isClearCache()) {
                for (File subdirectory : cacheDirectory.listFiles(File::isDirectory)) {
                    if (!subdirectory.getAbsolutePath().endsWith("mock")) {
                        boolean isDeleted = FileSystemUtils.deleteRecursively(subdirectory);
                        if (!isDeleted) {
                            throw new RuntimeException(String.format("Unable to clear cache directory %s!", subdirectory.getPath()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
            throws SolrServerException, IOException {

        if (!index.isCacheEnabled() ||
            index.isSchematize() ||
            index.isOnStartup() ||
            !request.getMethod().equals(org.apache.solr.client.solrj.SolrRequest.METHOD.GET) ||
            Objects.isNull(request)) {
            return client.request(request, collection);
        }

        // can return null and lint or code style rule may require using requestAttributes == null
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (Objects.isNull(requestAttributes)) {
            logger.warn("Unable to cache without originating request. RequestContextHolder.getRequestAttributes() is {}");
            return client.request(request, collection);
        }

        // get originating request from the request context holder request attributes
        HttpServletRequest originatingRequest = ((ServletRequestAttributes) requestAttributes).getRequest();

        // get a cache path from the path pattern
        String cachePath = ((String) originatingRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
            .replace(LEFT_CURLY_BRACKET, StringUtils.EMPTY)
            .replace(RIGHT_CURLY_BRACKET, StringUtils.EMPTY);

        // key as path in cache with UUID as cachePath/<UUID>.sdc => cacheLocation / cachePath / requestPath / <UUID>.sdc
        String key = String.format("%s%s/lookup_table", StringUtils.removeEnd(index.getCacheLocation(), FORWARD_SLASH), cachePath);

        File lookupFile = getOrCreateLookupFile(key);
        Map<String, String> innerMap = getOrCreateInnerMapForLookupFile(key, lookupFile);

        long start = System.currentTimeMillis();

        ObjectNode requestAsObject = requestToObjectNode(request);

        logger.info("{}: {} seconds", "DESERIALIZE", (System.currentTimeMillis() - start) / (double) 1000);

        long startClaimsToJWTUUID = System.currentTimeMillis();

        Map<String, Object> claims = requestObjectToClaims(requestAsObject);

        String jwt = jwtTokenService.createToken(collection, claims);

        byte[] bytes = jwt.getBytes(StandardCharsets.UTF_8);

        String uuid = UUID.nameUUIDFromBytes(bytes).toString();

        logger.info("{}:{}: {} seconds", uuid, "CLAIMS_TO_JWT_UUID", (System.currentTimeMillis() - startClaimsToJWTUUID) / (double) 1000);

        // don't allow response to return null
        NamedList<Object> response = null;

        File directory = new File(key);

        directory.mkdirs();

        String filename = String.format("%s/%s", key, uuid);

        File file = new File(filename);

        long startFoundCache, startQueryToSolr;

        if (file.exists()) { // cache response branch
            startFoundCache = System.currentTimeMillis();

            try {
                response = JavaObjectStorageFileUtility.readObject(filename);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(String.format("%s:%s", e.getClass(), e.getMessage(), e));
            }

            logger.info("{}:{}: {} seconds", uuid, "RESPONSE CACHED READ", (System.currentTimeMillis() - startFoundCache) / (double) 1000);
        } else { // actual response branch
            startQueryToSolr = System.currentTimeMillis();
            response = client.request(request, collection);

            JavaObjectStorageFileUtility.writeObject(response, filename);

            logger.info("{}:{}: {} seconds", uuid, "QUERY RESPONSE", (System.currentTimeMillis() - startQueryToSolr) / (double) 1000);

            long startUdateLookupTable = System.currentTimeMillis();

            innerMap.put(jwt, uuid);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(lookupFile, innerMap);

            logger.info("{}:{}: {} seconds", uuid, "UPDATE LOOKUP TABLE", (System.currentTimeMillis() - startUdateLookupTable) / (double) 1000);

            logger.info("{}:{}:{} {}", uuid, "REQUEST", uuid, requestAsObject.toPrettyString());
        }
        logger.info("{}:{}: {} seconds", uuid, "ACTUAL RESPONSE", (System.currentTimeMillis() - start) / (double) 1000);

        return response;
    }

    private synchronized File getOrCreateLookupFile(String key) {
        File lookupFile = this.lookup.get(key);

        if (lookupFile == null) {
            lookupFile = new File(key + ".sdc");
            this.lookup.put(key, lookupFile);
        }

        return lookupFile;
    }

    private synchronized Map<String, String> getOrCreateInnerMapForLookupFile(String key, File lookupFile) throws StreamReadException, DatabindException, IOException {
        Map<String, String> innerMap = this.map.get(key);

        if (innerMap == null) {
            innerMap = lookupFile.exists() && !lookupFile.isDirectory()
                    ? objectMapper.readValue(lookupFile, new TypeReference<ConcurrentHashMap<String, String>>() { })
                    : new ConcurrentHashMap<>();
            this.map.put(key, innerMap);
        }

        return innerMap;
    }

    /**
     * Convert the request to an ObjectNode with at most knowledge of SolrRequest at this time.
     * 
     * @param request SolrRequest
     * @return ObjectNode
     * @throws JsonMappingException when unable to map request parameters to JSON
     * @throws JsonProcessingException when unable to deserialize JSON parameters of the SolrRequest
     */
    private ObjectNode requestToObjectNode(SolrRequest<?> request) throws JsonProcessingException {
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

        if (Objects.nonNull(request.getParams())) {
            String jsonParams = request.getParams().jsonStr();
            if (Objects.nonNull(jsonParams)) {
                // can exit on exception
                ObjectNode params = (ObjectNode) objectMapper.readTree(jsonParams);
                rootNode.set(PARAMS, params);
            }
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

        return rootNode;
    }

    /**
     * Convert the ObjectNode to a Map<String, Object>.
     * 
     * @param rootNode ObjectNode from the SolrRequest
     * @return claims without nulls
     */
    private Map<String, Object> requestObjectToClaims(ObjectNode rootNode) {
        return ClaimBuilder
            .make()
            .with(BASE_PATH, rootNode.get(BASE_PATH))
            .with(BASIC_AUTH_USER, rootNode.get(BASIC_AUTH_USER))
            .with(BASIC_AUTH_PASSWORD, rootNode.get(BASIC_AUTH_PASSWORD))
            .with(COLLECTION, rootNode.get(COLLECTION))
            .with(HEADERS, rootNode.get(HEADERS))
            .with(METHOD, rootNode.get(METHOD))
            .with(PARAMS, rootNode.get(PARAMS))
            .with(PATH, rootNode.get(PATH))
            .with(PREFERRED_NODES, rootNode.get(PREFERRED_NODES))
            .with(QUERY_PARAMS, rootNode.get(QUERY_PARAMS))
            .with(REQUEST_TYPE, rootNode.get(REQUEST_TYPE))
            .getClaims();
    }

}
