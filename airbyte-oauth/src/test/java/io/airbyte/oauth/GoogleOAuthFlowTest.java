/*
 * MIT License
 *
 * Copyright (c) 2020 Airbyte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.airbyte.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.DestinationOAuthParameter;
import io.airbyte.config.SourceOAuthParameter;
import io.airbyte.config.persistence.ConfigNotFoundException;
import io.airbyte.config.persistence.ConfigRepository;
import io.airbyte.validation.json.JsonValidationException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoogleOAuthFlowTest {

  public static final String REDIRECT_URL = "https%3A//airbyte.io";
  private HttpClient httpClient;
  private ConfigRepository configRepository;
  private GoogleOAuthFlow googleOAuthFlow;

  private UUID workspaceId;
  private UUID definitionId;

  @BeforeEach
  public void setup() {
    httpClient = mock(HttpClient.class);
    configRepository = mock(ConfigRepository.class);
    googleOAuthFlow = new GoogleOAuthFlow(configRepository, httpClient);

    workspaceId = UUID.randomUUID();
    definitionId = UUID.randomUUID();
  }

  @Test
  public void testGetConsentUrlEmptyOAuthParameters() {
    assertThrows(ConfigNotFoundException.class,
        () -> googleOAuthFlow.getSourceConsentUrl(workspaceId, definitionId, REDIRECT_URL));
    assertThrows(ConfigNotFoundException.class,
        () -> googleOAuthFlow.getDestinationConsentUrl(workspaceId, definitionId, REDIRECT_URL));
  }

  @Test
  public void testGetConsentUrlIncompleteOAuthParameters() throws IOException, JsonValidationException {
    when(configRepository.listSourceOAuthParam()).thenReturn(List.of(new SourceOAuthParameter()
        .withOauthParameterId(UUID.randomUUID())
        .withSourceDefinitionId(definitionId)
        .withWorkspaceId(workspaceId)
        .withConfiguration(Jsons.emptyObject())));
    when(configRepository.listDestinationOAuthParam()).thenReturn(List.of(new DestinationOAuthParameter()
        .withOauthParameterId(UUID.randomUUID())
        .withDestinationDefinitionId(definitionId)
        .withWorkspaceId(workspaceId)
        .withConfiguration(Jsons.emptyObject())));
    assertThrows(IOException.class, () -> googleOAuthFlow.getSourceConsentUrl(workspaceId, definitionId, REDIRECT_URL));
    assertThrows(IOException.class, () -> googleOAuthFlow.getDestinationConsentUrl(workspaceId, definitionId, REDIRECT_URL));
  }

  @Test
  public void testSourceGetConsentUrl() throws IOException, ConfigNotFoundException, JsonValidationException {
    when(configRepository.listSourceOAuthParam()).thenReturn(List.of(new SourceOAuthParameter()
        .withOauthParameterId(UUID.randomUUID())
        .withSourceDefinitionId(definitionId)
        .withWorkspaceId(workspaceId)
        .withConfiguration(Jsons.jsonNode(ImmutableMap.builder()
            .put("client_id", "test")
            .build()))));
    final String actualSourceUrl = googleOAuthFlow.getSourceConsentUrl(workspaceId, definitionId, REDIRECT_URL);
    final String expectedSourceUrl = String.format(
        "https://accounts.google.com/o/oauth2/v2/auth?scope=%s&access_type=offline&include_granted_scopes=true&response_type=code&prompt=consent&state=%s&client_id=test&redirect_uri=%s",
        GoogleOAuthFlow.GOOGLE_ANALYTICS_SCOPE,
        definitionId,
        REDIRECT_URL);
    assertEquals(expectedSourceUrl, actualSourceUrl);
  }

  @Test
  public void testDestinationGetConsentUrl() throws IOException, ConfigNotFoundException, JsonValidationException {
    when(configRepository.listDestinationOAuthParam()).thenReturn(List.of(new DestinationOAuthParameter()
        .withOauthParameterId(UUID.randomUUID())
        .withDestinationDefinitionId(definitionId)
        .withWorkspaceId(workspaceId)
        .withConfiguration(Jsons.jsonNode(ImmutableMap.builder()
            .put("client_id", "test")
            .build()))));
    final String actualDestinationUrl = googleOAuthFlow.getDestinationConsentUrl(workspaceId, definitionId, REDIRECT_URL);
    final String expectedDestinationUrl = String.format(
        "https://accounts.google.com/o/oauth2/v2/auth?scope=%s&access_type=offline&include_granted_scopes=true&response_type=code&prompt=consent&state=%s&client_id=test&redirect_uri=%s",
        GoogleOAuthFlow.GOOGLE_ANALYTICS_SCOPE,
        definitionId,
        REDIRECT_URL);
    assertEquals(expectedDestinationUrl, actualDestinationUrl);
  }
}
