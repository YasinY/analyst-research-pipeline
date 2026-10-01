package com.assignment.research.evidence;

import java.util.List;

public interface SourceSearchPort {

    List<SearchHit> search(List<String> keywords, int maxResults);
}
