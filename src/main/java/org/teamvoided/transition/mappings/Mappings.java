package org.teamvoided.transition.mappings;

import java.util.List;
import java.util.Map;

public record Mappings(List<String> oldNamespaces, Map<String, String> oldToNewPaths) {}