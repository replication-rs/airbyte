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

package io.airbyte.integrations.destination.s3.jsonl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import alex.mojaki.s3upload.StreamTransferManager;
import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.commons.json.Jsons;
import io.airbyte.integrations.destination.s3.S3DestinationConfig;
import io.airbyte.integrations.destination.s3.S3FormatConfig;
import io.airbyte.integrations.destination.s3.csv.S3CsvFormatConfig.Flattening;
import io.airbyte.integrations.destination.s3.util.ConfigTestUtils;
import io.airbyte.integrations.destination.s3.util.S3StreamTransferManagerHelper;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("S3JsonlFormatConfig")
public class S3JsonlFormatConfigTest {

  @Test
  public void testHandlePartSizeConfig() throws IllegalAccessException {

    JsonNode config = ConfigTestUtils.getBaseConfig(Jsons.deserialize("{\n"
        + "  \"format_type\": \"JSONL\",\n"
        + "  \"part_size_mb\": 6\n"
        + "}"));

    S3DestinationConfig s3DestinationConfig = S3DestinationConfig
        .getS3DestinationConfig(config);
    ConfigTestUtils.assertBaseConfig(s3DestinationConfig);

    S3FormatConfig formatConfig = s3DestinationConfig.getFormatConfig();
    assertEquals("JSONL", formatConfig.getFormat().name());
    assertEquals(6, formatConfig.getPartSize());

    // Assert that is set properly in config
    StreamTransferManager streamTransferManager = S3StreamTransferManagerHelper.getDefault(
        s3DestinationConfig.getBucketName(), "objectKey", null,
        s3DestinationConfig.getFormatConfig().getPartSize());

    Integer partSizeBytes = (Integer) FieldUtils.readField(streamTransferManager, "partSize", true);
    assertEquals(6291456, partSizeBytes);
  }


  @Test
  public void testHandleAbsenceOfPartSizeConfig() throws IllegalAccessException {

    JsonNode config = ConfigTestUtils.getBaseConfig(Jsons.deserialize("{\n"
        + "  \"format_type\": \"JSONL\"\n"
        + "}"));

    S3DestinationConfig s3DestinationConfig = S3DestinationConfig
        .getS3DestinationConfig(config);
    ConfigTestUtils.assertBaseConfig(s3DestinationConfig);

    StreamTransferManager streamTransferManager = S3StreamTransferManagerHelper.getDefault(
        s3DestinationConfig.getBucketName(), "objectKey", null,
        s3DestinationConfig.getFormatConfig().getPartSize());

    Integer partSizeBytes = (Integer) FieldUtils.readField(streamTransferManager, "partSize", true);
    assertEquals(5242880, partSizeBytes); //5MB is a default value if nothing provided explicitly
  }
}
