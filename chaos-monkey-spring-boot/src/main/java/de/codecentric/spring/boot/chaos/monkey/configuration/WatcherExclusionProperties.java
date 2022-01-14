package de.codecentric.spring.boot.chaos.monkey.configuration;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WatcherExclusionProperties {
  private List<String> packages = Collections.emptyList();
  private List<String> classes = Collections.emptyList();
  private List<String> methods = Collections.emptyList();
}
