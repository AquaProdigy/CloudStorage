package org.example.cloudstorage.commandTest;

import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.CORSConfiguration;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioTest implements CommandLineRunner {
    private final MinioClient minioClient;

    @Override
    public void run(String... args) throws Exception {
//        minioClient.makeBucket(MakeBucketArgs.builder().bucket("test").build());

//        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket("minio").bucket("sex").build();
//        List<Bucket> bucketList = minioClient.listBuckets();
//        for (Bucket bucket : bucketList) {
//            System.out.println(bucket.name());
//        }

//        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
//                        .bucket("sex")
//                        .prefix("sosal/index")
//                .build());
//
//        for (Result<Item> result : results) {
//            Item item = result.get();
//            System.out.println(item.lastModified() + "\t" + item.size() + "\t" + item.objectName());
//        }

//        minioClient.putObject(PutObjectArgs.builder()
//                        .bucket("user-files")
//                .object("sex/")
//                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
//                .build());
    }
}
