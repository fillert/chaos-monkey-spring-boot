/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.codecentric.spring.boot.chaos.monkey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.codecentric.spring.boot.chaos.monkey.assaults.ExceptionAssault;
import de.codecentric.spring.boot.chaos.monkey.assaults.LatencyAssault;
import de.codecentric.spring.boot.chaos.monkey.component.ChaosMonkeyRequestScope;
import de.codecentric.spring.boot.chaos.monkey.component.MetricEventPublisher;
import de.codecentric.spring.boot.chaos.monkey.configuration.AssaultProperties;
import de.codecentric.spring.boot.chaos.monkey.configuration.ChaosMonkeySettings;
import de.codecentric.spring.boot.chaos.monkey.configuration.WatcherExclusionProperties;
import de.codecentric.spring.boot.chaos.monkey.configuration.WatcherProperties;
import de.codecentric.spring.boot.chaos.monkey.configuration.toggles.DefaultChaosToggleNameMapper;
import de.codecentric.spring.boot.chaos.monkey.configuration.toggles.DefaultChaosToggles;
import de.codecentric.spring.boot.demo.chaos.monkey.ChaosDemoApplication;
import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** @author Benjamin Wilms */
@SpringBootTest(
    classes = ChaosDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "chaos.monkey.watcher.controller=true",
      "chaos.monkey.assaults.level=1",
      "chaos.monkey.assaults.latencyRangeStart=10",
      "chaos.monkey.assaults.latencyRangeEnd=50",
      "chaos.monkey.assaults.killApplicationActive=true",
      "chaos.monkey.watcher.exclude.classes=example.chaos.monkey.chaosdemo.controller.HelloController",
      "spring.profiles.active=chaos-monkey"
    })

class ChaosDemoApplicationChaosMonkeyRequestScopeProfileTest {

  @Autowired private ChaosMonkeyRequestScope chaosMonkeyRequestScope;

  @Autowired private ChaosMonkeySettings monkeySettings;

  @Autowired private LatencyAssault latencyAssault;

  @Autowired private ExceptionAssault exceptionAssault;

  @Mock private MetricEventPublisher metricsMock;

  @BeforeEach
  void setUp() {
    chaosMonkeyRequestScope =
        new ChaosMonkeyRequestScope(
            monkeySettings,
            Arrays.asList(latencyAssault, exceptionAssault),
            Collections.emptyList(),
            metricsMock,
            new DefaultChaosToggles(),
            new DefaultChaosToggleNameMapper(
                monkeySettings.getChaosMonkeyProperties().getTogglePrefix()));
  }

  @Test
  void contextLoads() {
    assertNotNull(chaosMonkeyRequestScope);
  }

  @Test
  void checkChaosSettingsObject() {
    assertNotNull(monkeySettings);
  }

  @Test
  void checkChaosSettingsValues() {
    assertThat(monkeySettings.getChaosMonkeyProperties().isEnabled()).isFalse();
  }

  @Test
  void checkWatcherProperties() {
    SoftAssertions softAssertions = new SoftAssertions();
    WatcherProperties watcherProperties = monkeySettings.getWatcherProperties();

    softAssertions.assertThat(watcherProperties.isController()).isTrue();
    softAssertions.assertThat(watcherProperties.isRepository()).isFalse();
    softAssertions.assertThat(watcherProperties.isRestController()).isFalse();
    softAssertions.assertThat(watcherProperties.isService()).isFalse();

    softAssertions.assertAll();
  }

  @Test
  void checkAssaultProperties() {
    SoftAssertions softAssertions = new SoftAssertions();
    AssaultProperties assaultProperties = monkeySettings.getAssaultProperties();

    softAssertions.assertThat(assaultProperties.getLatencyRangeEnd()).isEqualTo(50);
    softAssertions.assertThat(assaultProperties.getLatencyRangeStart()).isEqualTo(10);
    softAssertions.assertThat(assaultProperties.getLevel()).isEqualTo(1);
    softAssertions.assertThat(assaultProperties.isLatencyActive()).isFalse();
    softAssertions.assertThat(assaultProperties.isExceptionsActive()).isFalse();
    softAssertions.assertThat(assaultProperties.isKillApplicationActive()).isTrue();
    softAssertions.assertThat(assaultProperties.getWatchedCustomServices()).isNull();

    softAssertions.assertAll();
  }

  @Test
  void checkExcludeProperties() {
    SoftAssertions softAssertions = new SoftAssertions();
    WatcherExclusionProperties watcherExclusionProperties =
        monkeySettings.getWatcherProperties().getExclude();

    softAssertions.assertThat(watcherExclusionProperties.getPackages()).isEmpty();
    softAssertions.assertThat(watcherExclusionProperties.getClasses().size()).isEqualTo(1);
    softAssertions.assertThat(watcherExclusionProperties.getMethods()).isEmpty();

    softAssertions.assertAll();
  }
}
