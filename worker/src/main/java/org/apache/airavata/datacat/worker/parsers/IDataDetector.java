package org.apache.airavata.datacat.worker.parsers;

import org.apache.airavata.datacat.commons.CatalogFileRequest;

public interface IDataDetector {

    boolean detectData(String localDirPath, CatalogFileRequest catalogFileRequest) throws Exception;

}
