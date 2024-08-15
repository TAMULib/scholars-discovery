package edu.tamu.scholars.middleware.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.service.JwtTokenService;

/**
 * 
 */
@Configuration
@Profile("!test")
public class SolrConfig {

    @Value("${spring.data.solr.host:http://localhost:8983/solr}")
    private String solrHost;

    @Bean
    @Order(2)
    public SolrClient solrClient() {
        return new Http2SolrClient.Builder(solrHost)
                .connectionTimeout(900000)
                .maxConnectionsPerHost(4)
                .build();
    }

    @Bean
    @Order(1)
    @Primary
    public SolrClient solrServer(SolrClient solrClient, IndexConfig index, JwtTokenService tokenService)
            throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        File lookupTable = new File("src/test/resources/lookup_table.sd");

        boolean fileValid = lookupTable.exists() && !lookupTable.isDirectory();

        // <JWT, UUID>
        final Map<String, String> map = fileValid ? mapper.readValue(lookupTable, new TypeReference<Map<String, String>>() { }) : new HashMap<>();

        return new SolrClient() {

            @Override
            public void close() throws IOException {
                solrClient.close();
            }

            @SuppressWarnings("unchecked")
            @Override
            public NamedList<Object> request(SolrRequest<?> request, String collection)
                    throws SolrServerException, IOException {

                if (Objects.isNull(request) || index.isSchematize() || index.isOnStartup()) {
                    return solrClient.request(request, collection);
                }

                ObjectNode rootNode = mapper.createObjectNode();

                rootNode.put("basePath", request.getBasePath());
                rootNode.put("basicAuthUser", request.getBasicAuthUser());
                rootNode.put("basicAuthPassword", request.getBasicAuthPassword());
                rootNode.put("collection", request.getCollection());

                if (Objects.nonNull(request.getHeaders())) {
                    ObjectNode headers = mapper.createObjectNode();
                    for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                        headers.put(header.getKey(), header.getValue());
                    }
                    rootNode.set("headers", headers);
                }

                rootNode.put("method", request.getMethod().toString());

                if (Objects.nonNull(request.getParams()) && Objects.nonNull(request.getParams().jsonStr())) {
                    ObjectNode params = (ObjectNode) mapper.readTree(request.getParams().jsonStr());
                    rootNode.set("params", params);
                }

                rootNode.put("path", request.getPath());

                if (Objects.nonNull(request.getPreferredNodes())) {
                    ArrayNode preferredNodes = mapper.createArrayNode();
                    for (String preferredNode : request.getPreferredNodes()) {
                        preferredNodes.add(preferredNode);
                    }
                    rootNode.set("preferredNodes", preferredNodes);
                }

                if (Objects.nonNull(request.getQueryParams())) {
                    ArrayNode queryParams = mapper.createArrayNode();
                    for (String queryParam : request.getQueryParams()) {
                        queryParams.add(queryParam);
                    }
                    rootNode.set("queryParams", queryParams);
                }

                rootNode.put("requestType", request.getRequestType());

                // System.out.println("getResponseParser: " + request.getResponseParser());
                // System.out.println("getUserPrincipal: " + request.getUserPrincipal());

                Map<String, Object> claims = mapper.readValue(rootNode.toPrettyString(), new TypeReference<Map<String, Object>>() { });

                String jwt = tokenService.createToken(collection, claims);

                byte[] bytes = jwt.getBytes(StandardCharsets.UTF_8);
                String uuid = UUID.nameUUIDFromBytes(bytes).toString();

                NamedList<Object> response = null;

                synchronized (map) {
                    map.put(jwt, uuid);

                    File directory = new File("src/test/resources/lookup_table");

                    directory.mkdir();

                    String filename = String.format("src/test/resources/lookup_table/%s", uuid);

                    File file = new File(filename);

                    if (file.exists()) {
                        try (
                            FileInputStream fileIn = new FileInputStream(filename);
                            ObjectInputStream in = new ObjectInputStream(fileIn);
                        ) {
                            response = (NamedList<Object>) in.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        response = solrClient.request(request, collection);
                        try (
                            FileOutputStream fileOut = new FileOutputStream(filename);
                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        ) {
                            out.writeObject(response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mapper.writerWithDefaultPrettyPrinter().writeValue(lookupTable, map);
                    }

                }

                return response;
            }

        };
    }

}
