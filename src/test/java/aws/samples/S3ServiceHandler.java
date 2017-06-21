package aws.samples;

import java.util.UUID;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3ServiceHandler {

    private final transient Logger logger = LoggerFactory.getLogger( S3ServiceHandler.class );

    private final String BUCKET_NAME = "paige-application";

    private final String IMAGE_CONTENT_TYPE = "image/jpeg";

    @Autowired
    private AmazonS3 amazonS3;

    public String uploadImage( File file, String folder ) {
        byte[] bytes = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream( file );
            bytes = IOUtils.toByteArray( new FileInputStream( file ) );
        } catch ( IOException e ) {
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        String path = serializeBytes( inputStream, bytes, folder );
        return getPhotoUrl( path );
    }

    private String serializeBytes( InputStream inputStream, byte[] bytes, String folder ) {
        if ( bytes != null ) {
            String fileName = generateFileName();
            String path = folder + File.separator + fileName;
            PutObjectRequest object = new PutObjectRequest( BUCKET_NAME, path, inputStream,
                                                            getObjectMetadata( bytes ) ).withCannedAcl( CannedAccessControlList.PublicRead );
            object.withSdkClientExecutionTimeout( 100000 );
            amazonS3.putObject( object ).getMetadata();
            return path;
        }
        throw new NullPointerException();
    }

    public String uploadImage( MultipartFile file, String folder ) {
        InputStream inputStream = null;
        String fileName = null;
        String path = null;
        byte[] bytes = null;
        try {
            inputStream = file.getInputStream();
            bytes = IOUtils.toByteArray( file.getInputStream() );
        } catch ( IOException e ) {
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        if ( bytes != null ) {
            fileName = generateFileName();
            path = folder + File.separator + fileName;
            PutObjectRequest object = new PutObjectRequest( BUCKET_NAME, path, inputStream,
                                                            getObjectMetadata( bytes ) ).withCannedAcl( CannedAccessControlList.PublicRead );
            object.withSdkClientExecutionTimeout( 100000 );
            amazonS3.putObject( object ).getMetadata();
        }
        return getPhotoUrl( path );
    }

    public S3Object getObject( String path ) {
        return amazonS3.getObject( new GetObjectRequest( BUCKET_NAME, path ) );
    }

    private void deleteObject( String path ) {
        amazonS3.deleteObject( new DeleteObjectRequest( BUCKET_NAME, path ) );
    }

    private ObjectMetadata getObjectMetadata( byte[] bytes ) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType( IMAGE_CONTENT_TYPE );
        metadata.setContentLength( (long) bytes.length );
        return metadata;
    }

    private String generateFileName() {
        return UUID.randomUUID().toString();
    }

    public String getPhotoUrl( String path ) {
        return getObject( path ).getObjectContent().getHttpRequest().getURI().toString();
    }

    public void createFolder( String folderName ) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength( 0 );
        InputStream emptyContent = new ByteArrayInputStream( new byte[0] );
        PutObjectRequest object = new PutObjectRequest( BUCKET_NAME, folderName, emptyContent, metadata );
        amazonS3.putObject( object );
    }

    public void deleteFolder( String folderName ) {
        amazonS3.listObjects( BUCKET_NAME, folderName ).getObjectSummaries()
                .forEach( objectSummary -> deleteObject( objectSummary.getKey() ) );
        amazonS3.deleteObject( BUCKET_NAME, folderName );
    }
}
