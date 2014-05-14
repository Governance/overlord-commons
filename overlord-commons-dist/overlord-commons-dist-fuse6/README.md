Installing into Fuse 6.x
========================

Run the following commands in the Fuse command line:

    features:addurl mvn:org.overlord/overlord-commons-dist-fuse6/2.0.1-SNAPSHOT/xml/features
    features:install -v overlord-commons-idp
    
If you don't want the actual IDP to be deployed but instead just
need the Overlord Commons Bundles installed, you can do this:

    features:addurl mvn:org.overlord/overlord-commons-dist-fuse6/2.0.1-SNAPSHOT/xml/features
    features:install -v overlord-commons
