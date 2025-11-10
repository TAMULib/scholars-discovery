package edu.tamu.scholars.middleware.config.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Injectable middleware configuration to specify export properties.
 * 
 * <p>See `middleware.export` in src/main/resources/application.yml.</p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.export")
public class ExportConfig {

    private String individualKey = "individual";

    private String individualBaseUri = "http://localhost:4200/display";

    private Map<String, String> personTypes = new HashMap<>();

    public String getIndividualKey() {
        return individualKey;
    }

    public void setIndividualKey(String individualKey) {
        this.individualKey = individualKey;
    }

    public String getIndividualBaseUri() {
        return individualBaseUri;
    }

    public void setIndividualBaseUri(String individualBaseUri) {
        this.individualBaseUri = individualBaseUri;
    }

    public Map<String, String> getPersonTypeMapping() {
        if (personTypeMapping == null || personTypeMapping.isEmpty()) {
            personTypeMapping = new HashMap<>();
            personTypeMapping.put("GraduateStudent", "Student Researcher");
            personTypeMapping.put("FacultyMember", "Faculty Member");
            personTypeMapping.put("NonFacultyAcademic", "Non Faculty Academic");
        }
        return personTypeMapping;
    }

    public void setPersonTypeMapping(Map<String, String> personTypeMapping) {
        this.personTypeMapping = personTypeMapping;
    }

}
