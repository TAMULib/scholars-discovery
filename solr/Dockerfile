FROM solr:9.9

USER root

COPY configsets/scholars-discovery/conf /opt/solr/server/solr/configsets/scholars-discovery/conf
COPY setup.sh /opt/solr/docker/scripts/setup.sh

# Remove golang - CVE-2023-24540, CVE-2023-24538, CVE-2024-24790, CVE-2025-22871
RUN apt-get update && \
    apt-get remove -y golang* && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /usr/local/go && \
    rm -rf /go && \
# Remove Avro from common Solr lib locations - CVE-2024-47561
    rm -f /opt/solr/server/lib/ext/avro-*.jar && \
    rm -f /opt/solr/contrib/*/lib/avro-*.jar && \
    rm -f /opt/solr/dist/solr-dataimporthandler-extras-*.jar 2>/dev/null || true && \
# Set ownership and permissions - configsets and setup script
    chown -R solr:solr /opt/solr/server/solr/configsets/scholars-discovery && \
    chmod -R 755 /opt/solr/server/solr/configsets/scholars-discovery && \
    chmod -R ugo+rx /opt/solr/docker/scripts/setup.sh

USER solr

CMD ["/bin/bash", "/opt/solr/docker/scripts/setup.sh"]
