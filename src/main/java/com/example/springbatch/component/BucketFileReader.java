package com.example.springbatch.component;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class BucketFileReader {

    public static String getBucketFile(String bucketName, String objectName) throws IOException {

        // Create a Storage instance (uses default credentials from GOOGLE_APPLICATION_CREDENTIALS)
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Fetch the Blob (file) from GCS
        Blob blob = storage.get(bucketName, objectName);

        if(blob == null) {
            return null;
        }
        log.info("Get files from bucket successfully ");
        return blob.getName();
    }
}
