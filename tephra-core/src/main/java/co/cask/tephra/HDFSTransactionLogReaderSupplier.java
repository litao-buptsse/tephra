/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.tephra;

import co.cask.tephra.persist.TransactionLogReader;
import com.google.common.base.Supplier;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * Provides the correct version of {@link TransactionLogReader}, based on the log's version metadata,
 * to read HDFS Transaction Logs.
 */
public class HDFSTransactionLogReaderSupplier implements Supplier<TransactionLogReader> {

  private final SequenceFile.Reader reader;
  private final String version;
  private AbstractHDFSLogReader logReader;

  public HDFSTransactionLogReaderSupplier(SequenceFile.Reader reader) {
    this.reader = reader;
    Text versionInfo = reader.getMetadata().get(new Text(TxConstants.TransactionLog.VERSION_KEY));
    this.version = versionInfo == null ? "v1" : versionInfo.toString();
  }

  @Override
  public TransactionLogReader get() {
    if (logReader != null) {
      return logReader;
    }

    switch (version) {
      case "v2" :
        logReader = new HDFSTransactionLogReaderV2(reader);
        return logReader;
      case "v1" :
        logReader = new HDFSTransactionLogReaderV1(reader);
        return logReader;
      default:
        throw new IllegalArgumentException(String.format("Invalid version %s found in the Transaction Log", version));
    }
  }
}
