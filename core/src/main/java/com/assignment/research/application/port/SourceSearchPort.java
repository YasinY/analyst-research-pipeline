package com.assignment.research.application.port;

import java.util.List;

public interface SourceSearchPort {

    List<SearchHit> search(List<String> keywords, int maxResults);
}
