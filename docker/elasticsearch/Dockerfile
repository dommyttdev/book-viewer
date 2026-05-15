ARG ELASTICSEARCH_VERSION=8.17.0
FROM docker.elastic.co/elasticsearch/elasticsearch:${ELASTICSEARCH_VERSION}

RUN elasticsearch-plugin install --batch analysis-kuromoji \
    && elasticsearch-plugin install --batch analysis-icu
